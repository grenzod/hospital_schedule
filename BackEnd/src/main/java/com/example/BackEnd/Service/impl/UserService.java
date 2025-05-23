package com.example.BackEnd.Service.impl;

import com.example.BackEnd.DTO.UserDTO;
import com.example.BackEnd.DTO.UserLoginDTO;
import com.example.BackEnd.Entity.EmailVerification;
import com.example.BackEnd.Entity.Role;
import com.example.BackEnd.Entity.User;
import com.example.BackEnd.Model.OtpMail;
import com.example.BackEnd.Repositories.mongodb.VerificationRepository;
import com.example.BackEnd.Repositories.mysql.RoleRepository;
import com.example.BackEnd.Repositories.mysql.UserRepository;
import com.example.BackEnd.Response.UserResponse;
import com.example.BackEnd.Service.IUserService;
import com.example.BackEnd.Service.IVerificationService;
import com.example.BackEnd.Utils.JWTTokenUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final OtpMailQueueService mailQueue;
    private final VerificationRepository verificationRepository;
    private final IVerificationService iVerificationService;

    @Override
    public void createVerification(UserDTO userDTO) {
        String otp = UUID.randomUUID()
                       .toString()      
                       .replace("-", "") 
                       .substring(0, 6)  
                       .toUpperCase();   
        mailQueue.enqueue(new OtpMail(userDTO.getEmail(), otp));
        iVerificationService.createOne(userDTO.getEmail(), otp);
    }

    @Override
    public User retrievePass(UserDTO userDTO, String code) throws Exception {
        EmailVerification emailVerification = verificationRepository.findByEmail(userDTO.getEmail());
        if (!emailVerification.getCode().equals(code)) {
            throw new Exception("Your Verification code is not true ");
        }

        User existingUser =
                userRepository.findByEmail(userDTO.getEmail()).orElseThrow(() -> new Exception("Not found User"));
        existingUser.setPassword(userDTO.getPassword());

        return userRepository.save(existingUser);
    }

    @Override
    public User createUser(UserDTO userDTO, String code) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        String email = userDTO.getEmail();
        userDTO.setRoleId(3);

        EmailVerification emailVerification = verificationRepository.findByEmail(userDTO.getEmail());
        if(!emailVerification.getCode().equals(code)) {
            throw new Exception("Your Verification code is not true ");
        }

        if(!email.isBlank() && userRepository.existsByEmail(email)){
            throw new DataIntegrityViolationException("Email is already in use");
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        if(role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new Exception("You cannot register as an admin account");
        }

        User user = User.builder()
                .fullName(userDTO.getFullName())
                .gender(userDTO.getGender())
                .phoneNumber(phoneNumber)
                .email(email)
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .isActive(true)
                .build();
        user.setRole(role);
        verificationRepository.delete(emailVerification);

        return userRepository.save(user);
    }

    @Override
    public String login(UserLoginDTO userLoginDTO) throws Exception {
        String email = userLoginDTO.getEmail();
        String password = userLoginDTO.getPassword();

        Optional<User> user = Optional.empty();
        String subject = null;

        if(email != null && userRepository.existsByEmail(email)) {
            user = userRepository.findByEmail(email);
            subject = email;
        }

        if(user.isEmpty()){
            throw new DataIntegrityViolationException("Your information is incorrect");
        }

        User existingUser = user.get();
        if(!passwordEncoder.matches(password, existingUser.getPassword())){
            throw new BadCredentialsException("Wrong password or phone number !!");
        }

        if(existingUser.getRole() == null){
            throw new Exception("Role not found");
        }
        if(!user.get().getIsActive()){
            throw new Exception("This account is not active");
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        subject,
                        password,
                        existingUser.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenUtil.generateToken(existingUser);
    }

    @Override
    public User getUserDetailFromToken(String extraStringToken) throws Exception {
        if(jwtTokenUtil.isTokenExpired(extraStringToken)){
            throw new Exception("Expired or invalid token");
        }
        String subject = jwtTokenUtil.extractSubject(extraStringToken);
        Optional<User> user = userRepository.findByEmail(subject);

        if(user.isPresent()){
            return user.get();
        }
        else{
            throw new Exception("Invalid token");
        }
    }

    @Override
    public User updateUser(UserDTO userDTO, Integer userId, String file) throws Exception {
        User existingUser =
                userRepository.findById(userId).orElseThrow(() -> new Exception("Not found User"));
        if(userRepository.findByPhoneNumber(userDTO.getPhoneNumber()).isPresent()
                && !Objects.equals(existingUser.getPhoneNumber(), userDTO.getPhoneNumber())){
            throw new Exception("This Phone have been used !!");
        }

        if(userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()){
            String newPassword = userDTO.getPassword();
            newPassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(newPassword);
        }

        if(userDTO.getFullName() != null) existingUser.setFullName(userDTO.getFullName());
        if(userDTO.getDateOfBirth() != null) existingUser.setDateOfBirth(userDTO.getDateOfBirth());
        if(userDTO.getGender() != null) existingUser.setGender(userDTO.getGender());
        if(userDTO.getAddress() != null) existingUser.setAddress(userDTO.getAddress());
        if(userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if(file != null) existingUser.setAvatarUrl(file);
        if(
            userDTO.getPassword() != null && 
            userDTO.getRetypePassword() != null && 
            userDTO.getPassword().equals(existingUser.getPassword())
        )
            existingUser.setPassword(userDTO.getRetypePassword());

        return userRepository.save(existingUser);
    }

    @Override
    public User deleteUser(Integer userId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new Exception("Not found User"));
        if(user.getIsActive()) user.setIsActive(false);
        else user.setIsActive(true);
        return userRepository.save(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(String keyword, PageRequest pageRequest) {
        Page<User> usersPage = userRepository.searchUsers(keyword, pageRequest);
        return usersPage.map(UserResponse::fromUser);
    }

    @Override
    public User changePassword(UserDTO userDTO, Integer userId) throws Exception {
        User existingUser =
                userRepository.findById(userId).orElseThrow(() -> new Exception("Not found User"));
        if(!passwordEncoder.matches(userDTO.getPassword(), existingUser.getPassword())){
            throw new BadCredentialsException("Wrong password or phone number !!");
        }

        String newPassword = userDTO.getRetypePassword();
        newPassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(newPassword);

        return userRepository.save(existingUser);
    }

}

