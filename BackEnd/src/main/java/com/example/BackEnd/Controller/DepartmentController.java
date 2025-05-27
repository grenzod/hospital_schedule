package com.example.BackEnd.Controller;

import com.example.BackEnd.DTO.DepartmentDTO;
import com.example.BackEnd.Entity.Department;
import com.example.BackEnd.Response.DepartmentResponse;
import com.example.BackEnd.Response.ObjectListResponse;
import com.example.BackEnd.Service.IDepartmentService;
import com.example.BackEnd.Utils.StoreFileUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final IDepartmentService iDepartmentService;

    @GetMapping("")
    public ResponseEntity<?> getDepartments(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(name = "status", required = false) Boolean status
    ){
        PageRequest pageRequest = PageRequest.of(
                page,
                limit,
                Sort.by("id").descending()
        );
        Page<DepartmentResponse> departmentPage = iDepartmentService.getAllDepartments(keyword, status, pageRequest);
        int totalPages = departmentPage.getTotalPages();
        List<DepartmentResponse> departmentList = departmentPage.getContent();

        return ResponseEntity.ok().body(ObjectListResponse.builder()
                    .objects(departmentList)
                    .total(totalPages)
                .build());
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addDepartment(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @Validated @RequestParam("file") MultipartFile file)
    {
        try {
            if (file.isEmpty()) {
                throw new Exception("File is empty");
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File is too large! Maximum size is 10 MB");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("File must be an image!");
            }

            String imageFilename = StoreFileUtil.storeFile(file, "departments");
            DepartmentDTO departmentDTO = DepartmentDTO.builder()
                    .name(name)
                    .description(description)
                    .build();
            Department department = iDepartmentService.addDepartment(departmentDTO, imageFilename);
            return ResponseEntity.ok(department);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartmentById(@PathVariable("id") Integer id){
        iDepartmentService.deleteDepartmentById(id);
        return ResponseEntity.ok().body(Map.of(
                "message", "Successfully"
        ));
    }
}
