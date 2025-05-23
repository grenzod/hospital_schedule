package com.example.BackEnd.Service;

import com.example.BackEnd.DTO.UserDTO;
import com.example.BackEnd.DTO.UserLoginDTO;
import com.example.BackEnd.Entity.User;
import com.example.BackEnd.Response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IUserService {
    void createVerification(UserDTO userDTO);
    User retrievePass(UserDTO userDTO, String code) throws Exception; 
    User createUser(UserDTO userDTO, String code) throws Exception;
    String login(UserLoginDTO userLoginDTO) throws Exception;
    User getUserDetailFromToken(String extraStringToken) throws Exception;
    User updateUser(UserDTO userDTO, Integer userId, String file) throws Exception;
    User deleteUser(Integer userId) throws Exception;
    Page<UserResponse> getAllUsers(String keyword, PageRequest pageRequest);
    User changePassword(UserDTO userDTO, Integer userId) throws Exception;
}
