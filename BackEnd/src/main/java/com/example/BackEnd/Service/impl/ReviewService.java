package com.example.BackEnd.Service.impl;

import com.example.BackEnd.Entity.Doctor;
import com.example.BackEnd.Entity.Review;
import com.example.BackEnd.Entity.User;
import com.example.BackEnd.Repositories.mysql.DoctorRepository;
import com.example.BackEnd.Repositories.mysql.ReviewRepository;
import com.example.BackEnd.Repositories.mysql.UserRepository;
import com.example.BackEnd.Response.ReviewResponse;
import com.example.BackEnd.Service.IReviewService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public Page<ReviewResponse> getAllReviewByDoctorId(PageRequest pageRequest, Integer doctorId) {
        Page<Review> reviewPage = reviewRepository.findByDoctorId(doctorId, pageRequest);
        return reviewPage.map(ReviewResponse::fromReview);
    }

    @Override
    public synchronized Review comment(Integer userId, Integer doctorId, String comment) throws Exception{
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));

        Review review = Review.builder()
                .comment(user.getFullName() + " feed back about " + doctor.getUser().getFullName() + " as: " + comment)
                .user(user)
                .doctor(doctor)
                .build();
        return reviewRepository.save(review);
    }
}
