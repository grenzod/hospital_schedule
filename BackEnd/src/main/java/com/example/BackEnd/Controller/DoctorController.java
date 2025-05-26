package com.example.BackEnd.Controller;

import com.example.BackEnd.DTO.DoctorDTO;
import com.example.BackEnd.Entity.Doctor;
import com.example.BackEnd.Response.DoctorResponse;
import com.example.BackEnd.Response.ObjectListResponse;
import com.example.BackEnd.Service.IDoctorService;
import com.example.BackEnd.Utils.StoreFileUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final IDoctorService iDoctorService;

    @GetMapping("")
    public ResponseEntity<?> getAllDoctors(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "department", required = false) Integer departmentId,
            @RequestParam(name = "status", required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
        ) {
        PageRequest pageRequest = PageRequest.of(
            page, 
            limit, 
            Sort.by("id").descending());
        Page<DoctorResponse> doctors = iDoctorService.getAllDoctors(keyword, departmentId, status, pageRequest);
        
        return ResponseEntity.ok(ObjectListResponse.builder()
                .objects(doctors.getContent())
                .total(doctors.getTotalPages())
                .build());
    }

    @GetMapping("/getByDepartment/{id}")
    public ResponseEntity<?> getDoctorsByDepartmentId(@PathVariable("id") Integer id,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int limit) {
        PageRequest pageRequest = PageRequest.of(
                page,
                limit,
                Sort.by("id").descending()
        );
        Page<DoctorResponse> doctors = iDoctorService.getDoctorsByDepartmentId(id, pageRequest);
        int totalPages = doctors.getTotalPages();
        List<DoctorResponse> doctorResponseList = doctors.getContent();

        return ResponseEntity.ok().body(ObjectListResponse.builder()
                        .objects(doctorResponseList)
                        .total(totalPages)
                .build());
    }

    @GetMapping("/getDoctor/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable("id") Integer id) {
        DoctorResponse doctor = iDoctorService.getDoctorById(id);
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }
        return ResponseEntity.ok(doctor);
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addDoctor(
            @RequestParam("ids") String ids,
            @RequestParam("number") String licenseNumber,
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
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

            List<Integer> processIds = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .toList();
            Integer userId = processIds.get(0);
            Integer departmentId = processIds.get(1);

            String imageFilename = StoreFileUtil.storeFile(file, "doctors");
            Doctor doctor = iDoctorService.addDoctor(userId, departmentId, imageFilename, licenseNumber);
            return ResponseEntity.ok().body(doctor);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid ID format");
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.badRequest().body("IDs must contain exactly 2 values");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDoctor(
            @PathVariable("id") Integer id,
            @RequestPart("doctor") DoctorDTO doctorDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            String imageFilename = null;
            if (file != null && !file.isEmpty()) {
                if (file.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File is too large! Maximum size is 10 MB");
                }
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body("File must be an image!");
                }
                imageFilename = StoreFileUtil.storeFile(file, "doctors");
            }

            Doctor doctor = iDoctorService.upgradeDoctor(id, doctorDTO, imageFilename);
            return ResponseEntity.ok().body(doctor);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable("id") Integer id){
            iDoctorService.deleteDoctorById(id);
            return ResponseEntity.ok().body("Chuyển đổi trạng thái thành công");
    }
}
