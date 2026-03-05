package jh.exp.process.service.controller;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.process.core.entity.req.ProcessDriveReq;
import jh.exp.process.core.entity.res.ProcessDriveRes;
import jh.exp.process.service.driver.ProcessCommandDriver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drive")
@RequiredArgsConstructor
public class ProcessDriveController {

    private final ProcessCommandDriver processCommandDriver;

    @PostMapping
    public ApiResponse<ProcessDriveRes> execute(@RequestBody ProcessDriveReq req) {
        return ApiResponse.success(processCommandDriver.execute(req));
    }
}
