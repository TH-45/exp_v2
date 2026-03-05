

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.nio.file.Paths;

/**
 * RustFS S3 协议调用示例
 * 依赖库：software.amazon.awssdk:s3
 */
public class RustfsS3Example {
    public static void main(String[] args) {

        // --- 1. 初始化 S3 客户端 ---
        // RustFS 兼容 S3 标准协议，因此可以使用 AWS SDK 进行连接
        S3Client s3 = S3Client.builder()
                // 替换为你的 Ubuntu 虚拟机 IP 地址
                .endpointOverride(URI.create("http://192.168.101.128:9000"))
                // RustFS 本身不验证区域，但 SDK 要求必须提供一个合法的 Region 格式
                .region(Region.US_EAST_1)
                // 设置访问凭证：对应 docker-compose 中的 RUSTFS_ACCESS_KEY 和 RUSTFS_SECRET_KEY
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("rustfsadmin", "rustfsadmin")
                        )
                )
                // 关键配置：强制使用路径样式访问（Path-style），RustFS 不支持虚拟主机样式（Virtual-host-style）
                .forcePathStyle(true)
                .build();

        // 存储桶（Bucket）名称
        String bucket = "my-bucket";

        try {
            // --- 2. 创建存储桶 ---
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            System.out.println("成功创建存储桶: " + bucket);
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            // 如果存储桶已存在，捕获异常并继续
            System.out.println("存储桶已存在，跳过创建步骤。");
        }

        // --- 3. 上传文件 ---
        // 将本地当前目录下的 hello.txt 上传到 RustFS 中
        s3.putObject(
                PutObjectRequest.builder().bucket(bucket).key("hello.txt").build(),
                Paths.get("D:\\ieda_deta\\exp_v2\\exp-common\\exp-common-service\\src\\test\\java\\hello.txt")
        );
        System.out.println("文件上传成功: hello.txt");

        // --- 4. 下载文件 ---
        // 从 RustFS 获取文件并保存为本地的 downloaded-hello.txt
        s3.getObject(
                GetObjectRequest.builder().bucket(bucket).key("hello.txt").build(),
                Paths.get("downloaded-hello.txt")
        );
        System.out.println("文件下载成功: downloaded-hello.txt");

        // --- 5. 列出存储桶中的对象 ---
        System.out.println("正在获取对象列表...");
        ListObjectsV2Response listResponse = s3.listObjectsV2(
                ListObjectsV2Request.builder().bucket(bucket).build()
        );
        listResponse.contents().forEach(obj ->
                System.out.println("发现对象: " + obj.key() + " (大小: " + obj.size() + " bytes)")
        );

        // --- 6. 删除对象 ---
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key("hello.txt").build());
        System.out.println("已从存储桶中删除对象: hello.txt");

        // --- 7. 删除存储桶 (可选) ---
        // 注意：只有当存储桶为空时才能被删除
        // s3.deleteBucket(DeleteBucketRequest.builder().bucket(bucket).build());
        // System.out.println("已删除存储桶: " + bucket);

        // 最后记得关闭客户端
        s3.close();
    }
}