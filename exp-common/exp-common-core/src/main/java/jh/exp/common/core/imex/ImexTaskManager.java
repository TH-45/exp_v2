package jh.exp.common.core.imex;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ImexTaskManager implements DisposableBean {
    @Value("${exp.imex.error-preview-limit:100}")
    private int errorPreviewLimit;
    @Value("${exp.imex.task-retention-hours:24}")
    private long taskRetentionHours;

    private final Map<String, TaskState> taskStore = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ImexTaskSubmitRes createTask(String bizCode, ImexTaskType taskType) {
        cleanupExpired();
        String taskId = UUID.randomUUID().toString().replace("-", "");
        TaskState state = new TaskState(taskId, bizCode, taskType);
        taskStore.put(taskId, state);
        return new ImexTaskSubmitRes(taskId);
    }

    public ImexTaskResult queryTask(String taskId) {
        cleanupExpired();
        TaskState state = taskStore.get(taskId);
        if (state == null) {
            return null;
        }
        synchronized (state) {
            return state.toResult();
        }
    }

    public byte[] queryExportFileBytes(String taskId) {
        TaskState state = taskStore.get(taskId);
        if (state == null) {
            return null;
        }
        synchronized (state) {
            return state.exportFileBytes;
        }
    }

    public String queryExportContentType(String taskId) {
        TaskState state = taskStore.get(taskId);
        if (state == null) {
            return null;
        }
        synchronized (state) {
            return state.exportContentType;
        }
    }

    public void runAsync(String taskId, Consumer<TaskContext> consumer) {
        TaskState state = taskStore.get(taskId);
        if (state == null) {
            return;
        }
        executor.submit(() -> {
            synchronized (state) {
                state.status = ImexTaskStatus.RUNNING;
                state.updatedTime = LocalDateTime.now();
            }
            try {
                consumer.accept(new TaskContext(state));
                synchronized (state) {
                    if (state.status == ImexTaskStatus.RUNNING) {
                        if (state.failedRows > 0 && state.successRows > 0) {
                            state.status = ImexTaskStatus.PARTIAL_SUCCESS;
                        } else if (state.failedRows > 0) {
                            state.status = ImexTaskStatus.FAILED;
                        } else {
                            state.status = ImexTaskStatus.SUCCESS;
                        }
                        state.updatedTime = LocalDateTime.now();
                    }
                }
            } catch (Exception ex) {
                synchronized (state) {
                    state.status = ImexTaskStatus.FAILED;
                    state.message = ex.getMessage() == null ? "任务执行失败" : ex.getMessage();
                    state.updatedTime = LocalDateTime.now();
                }
            }
        });
    }

    private void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        Duration retention = Duration.ofHours(Math.max(taskRetentionHours, 1L));
        taskStore.entrySet().removeIf(entry -> {
            TaskState state = entry.getValue();
            synchronized (state) {
                return Duration.between(state.updatedTime, now).compareTo(retention) > 0;
            }
        });
    }

    @Override
    public void destroy() {
        executor.shutdown();
    }

    public class TaskContext {
        private final TaskState state;

        TaskContext(TaskState state) {
            this.state = state;
        }

        public void setTotalRows(int totalRows) {
            synchronized (state) {
                state.totalRows = Math.max(totalRows, 0);
                state.updatedTime = LocalDateTime.now();
            }
        }

        public void addSuccess(int count) {
            synchronized (state) {
                state.successRows += Math.max(count, 0);
                state.updatedTime = LocalDateTime.now();
            }
        }

        public void addFailure(Integer rowNo, String errorType, String message) {
            synchronized (state) {
                state.failedRows += 1;
                if (state.errorPreview.size() < errorPreviewLimit) {
                    state.errorPreview.add(new ImexTaskErrorItem(rowNo, safe(errorType), safe(message)));
                } else {
                    state.errorOverflowCount += 1;
                }
                state.updatedTime = LocalDateTime.now();
            }
        }

        public void setMessage(String message) {
            synchronized (state) {
                state.message = message;
                state.updatedTime = LocalDateTime.now();
            }
        }

        public void setExportFile(String fileName, String contentType, byte[] bytes) {
            synchronized (state) {
                state.exportFileName = fileName;
                state.exportContentType = contentType;
                state.exportFileBytes = bytes;
                state.updatedTime = LocalDateTime.now();
            }
        }

        public void markFailed(String message) {
            synchronized (state) {
                state.status = ImexTaskStatus.FAILED;
                state.message = message;
                state.updatedTime = LocalDateTime.now();
            }
        }

        private static String safe(String input) {
            return input == null ? "" : input;
        }
    }

    private static class TaskState {
        private final String taskId;
        private final String bizCode;
        private final ImexTaskType taskType;

        private ImexTaskStatus status = ImexTaskStatus.PENDING;
        private int totalRows = 0;
        private int successRows = 0;
        private int failedRows = 0;
        private int errorOverflowCount = 0;
        private String message = "";
        private String exportFileName;
        private String exportContentType;
        private byte[] exportFileBytes;
        private final List<ImexTaskErrorItem> errorPreview = new ArrayList<>();
        private final LocalDateTime createdTime = LocalDateTime.now();
        private LocalDateTime updatedTime = LocalDateTime.now();

        TaskState(String taskId, String bizCode, ImexTaskType taskType) {
            this.taskId = taskId;
            this.bizCode = bizCode;
            this.taskType = taskType;
        }

        ImexTaskResult toResult() {
            ImexTaskResult result = new ImexTaskResult();
            result.setTaskId(taskId);
            result.setBizCode(bizCode);
            result.setTaskType(taskType);
            result.setStatus(status);
            result.setTotalRows(totalRows);
            result.setSuccessRows(successRows);
            result.setFailedRows(failedRows);
            result.setErrorOverflowCount(errorOverflowCount);
            result.setMessage(message);
            result.setExportFileName(exportFileName);
            result.setDownloadable(Objects.nonNull(exportFileBytes) && exportFileBytes.length > 0);
            result.setCreatedTime(createdTime);
            result.setUpdatedTime(updatedTime);
            result.setErrorPreview(new ArrayList<>(errorPreview));
            return result;
        }
    }
}
