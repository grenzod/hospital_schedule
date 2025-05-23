package com.example.BackEnd.Service;

import com.example.BackEnd.Entity.Review;
import com.example.BackEnd.Response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IReviewService {
    Page<ReviewResponse> getAllReviewByDoctorId(PageRequest pageRequest, Integer doctorId);
    Review comment(Integer userId, Integer doctorId, String comment) throws Exception;
}
