package com.example.BackEnd.Controller;

import com.example.BackEnd.DTO.ObjectStatsDTO;
import com.example.BackEnd.Entity.Review;
import com.example.BackEnd.Repositories.mysql.ReviewRepository;
import com.example.BackEnd.Response.ObjectListResponse;
import com.example.BackEnd.Response.ReviewResponse;
import com.example.BackEnd.Service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final IReviewService iReviewService;
    private final ReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<?> getAllReviewsByDoctorId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam("doctorId") Integer doctorId
    ){
        try {
            PageRequest pageRequest = PageRequest.of(
                    page,
                    limit,
                    Sort.by("id").descending()
            );
            Page<ReviewResponse> reviews = iReviewService.getAllReviewByDoctorId(pageRequest, doctorId);
            int totalPages = reviews.getTotalPages();
            List<ReviewResponse> reviewResponseList = reviews.getContent();

            return ResponseEntity.ok().body(ObjectListResponse.builder()
                            .objects(reviewResponseList)
                            .total(totalPages)
                    .build());
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/analyze")
    public List<ObjectStatsDTO> newReviewsLastMonths(
            @RequestParam(defaultValue = "6") int months) {
        LocalDateTime start = LocalDateTime.now()
                .minusMonths(months - 1)
                .withDayOfMonth(1)
                .toLocalDate().atStartOfDay();
        return reviewRepository.countNewCommentsPerMonthSince(start);
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> comment(
            @RequestParam("userId") Integer userId,
            @RequestParam("doctorId") Integer doctorId,
            @RequestParam("comment") String comment
    ){
        try{
            Review review = iReviewService.comment(userId, doctorId, comment);
            return ResponseEntity.ok().body(review);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable(name = "id") Integer id
    ){
        try{
            reviewRepository.deleteById(id);
            return ResponseEntity.ok().body("Xóa thành công");
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
