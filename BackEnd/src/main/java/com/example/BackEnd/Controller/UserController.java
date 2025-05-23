package com.example.BackEnd.Controller;

import com.example.BackEnd.DTO.ObjectStatsDTO;
import com.example.BackEnd.DTO.UserDTO;
import com.example.BackEnd.DTO.UserLoginDTO;
import com.example.BackEnd.Entity.User;
import com.example.BackEnd.Repositories.mysql.UserRepository;
import com.example.BackEnd.Response.LoginResponse;
import com.example.BackEnd.Response.ObjectListResponse;
import com.example.BackEnd.Response.UserResponse;
import com.example.BackEnd.Service.ITokenService;
import com.example.BackEnd.Service.IUserService;
import com.example.BackEnd.Service.impl.UserService;
import com.example.BackEnd.Utils.StoreFileUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService iUserService;
    private final UserService userService;
    private final ITokenService iTokenService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ){
        PageRequest pageRequest = PageRequest.of(
                page,
                limit,
                Sort.by("id").descending()
        );
        Page<UserResponse> userPages = iUserService.getAllUsers(keyword, pageRequest);
        int totalPages = userPages.getTotalPages();
        List<UserResponse> users = userPages.getContent();

        return ResponseEntity.ok(ObjectListResponse.builder()
                .objects(users)
                .total(totalPages)
                .build());
    }

    @GetMapping("/analyze")
    public List<ObjectStatsDTO> newUsersLastMonths(
            @RequestParam(defaultValue = "6") int months
        ) {
        LocalDateTime start = LocalDateTime.now()
                .minusMonths(months - 1)
                .withDayOfMonth(1)
                .toLocalDate().atStartOfDay();
        return userRepository.countNewUsersPerMonthSince(start);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO,
                                    BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }

            if(userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()) {
                return ResponseEntity.badRequest().body("You must enter your email");
            }

            iUserService.createVerification(userDTO);
            return ResponseEntity.ok().body(Map.of("message", "Hãy kiểm tra hộp thư email của bạn"));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body("Có lỗi" + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> addUser(@Valid @RequestBody UserDTO userDTO,
                                    @RequestParam String code,
                                    BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }

            if(userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()) {
                return ResponseEntity.badRequest().body("You must enter your email");
            }

            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body("RetypedPassword don't match to Password");
            }
            User user = iUserService.createUser(userDTO, code);
            return ResponseEntity.ok().body(user);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/retrieve")
    public ResponseEntity<?> retrievePassword(@Valid @RequestBody UserDTO userDTO,
                                    @RequestParam String code,
                                    BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }

            if(userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()) {
                return ResponseEntity.badRequest().body("You must enter your email");
            }

            User user = iUserService.retrievePass(userDTO, code);
            return ResponseEntity.ok().body(user);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> logUser(@Valid @RequestBody UserLoginDTO userLoginDTO,
                                                 HttpServletRequest request) {
        try {
            String token = iUserService.login(userLoginDTO);
            String userAgent = request.getHeader("X-User-Agent");
            User user = iUserService.getUserDetailFromToken(token);
            iTokenService.addToken(user, token, isMobileDev(userAgent));

            return ResponseEntity.ok().body(LoginResponse.builder()
                    .message("Login Successfully")
                    .token(token)
                    .build());
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .message("Login Fail" + e.getMessage())
                    .token(null)
                    .build());
        }
    }

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization){
        try {
            String token = authorization.substring(7);
            return ResponseEntity.ok().body(iTokenService.deleteToken(token));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/details")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String authorization) {
        try {
            String extraStringToken = authorization.substring(7);
            User user = userService.getUserDetailFromToken(extraStringToken);
            return ResponseEntity.ok(UserResponse.fromUser(user));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/updates/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer userId,    
            @RequestPart(name = "user") UserDTO userDTO,
            @RequestPart(required = false, name = "file") MultipartFile file,
            @RequestHeader("Authorization") String authorization
    ){
        try {
            String extraStringToken = authorization.substring(7);
            User user = userService.getUserDetailFromToken(extraStringToken);
            if(!Objects.equals(user.getId(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            String fileName = null;
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
                fileName = StoreFileUtil.storeFile(file, "users");
            }

            User userUpdate = userService.updateUser(userDTO, userId, fileName);
            return ResponseEntity.ok().body(UserResponse.fromUser(userUpdate));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-password/{userId}")
    public ResponseEntity<?> changePassword(
            @PathVariable Integer userId,
            @RequestBody UserDTO userDTO,
            @RequestHeader("Authorization") String authorization
    ){
        try {
            String extraStringToken = authorization.substring(7);
            User user = userService.getUserDetailFromToken(extraStringToken);
            if(!Objects.equals(user.getId(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body("RetypedPassword don't match to Password");
            }
            User userUpdate = userService.changePassword(userDTO, userId);
            return ResponseEntity.ok().body(UserResponse.fromUser(userUpdate));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("toggle/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId){
        try {
            User userUpdate = userService.deleteUser(userId);
            return ResponseEntity.ok().body(UserResponse.fromUser(userUpdate));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private boolean isMobileDev(String UserAgent){
        return UserAgent.toLowerCase().contains("mobile");
    }
}

