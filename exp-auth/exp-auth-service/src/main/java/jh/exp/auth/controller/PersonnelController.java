package jh.exp.auth.controller;

import jakarta.validation.Valid;
import jh.exp.auth.entity.Position;
import jh.exp.auth.inter.PositionService;
import jh.exp.common.api.ApiResponse;
import jh.exp.common.api.PageRequest;
import jh.exp.common.api.PageResult;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/personnel")
public class PersonnelController {

    @Autowired
    private PositionService positionService;

    @PostMapping("/list")
    public ApiResponse<PageResult<Position>> queryPersonnel(@RequestBody @Valid PageRequest PageRequest) {

        Page<Position> positions = positionService.queryPosition(PageRequest);

        return null;
    }

}
