package com.debbiecyber.QuickBite.controller;


import com.debbiecyber.QuickBite.dto.response.APIResponse;
import com.debbiecyber.QuickBite.dto.response.ReviewResponse;
import com.debbiecyber.QuickBite.dto.response.PageResponse;
import com.debbiecyber.QuickBite.dto.resquest.ReviewRequest;
import com.debbiecyber.QuickBite.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;


    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<ReviewResponse>> leaveAReview(@Valid @RequestBody ReviewRequest reviewRequest, @AuthenticationPrincipal UserDetails userDetails) {
        ReviewResponse reviewResponse = reviewService.leaveAReview(reviewRequest, userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(APIResponse.success("Review submitted successfully", reviewResponse));
    }


    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<APIResponse<PageResponse<ReviewResponse>>> getAllRestaurantReviews(@PathVariable Long restaurantId, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ReviewResponse> reviewResponseList = reviewService.getRestaurantReviews(restaurantId, pageable);

        return ResponseEntity.ok(APIResponse.success("Reviews fetched successfully", reviewResponseList));
    }


    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<PageResponse<ReviewResponse>>> getAllMyReviews(@AuthenticationPrincipal UserDetails userDetails, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ReviewResponse> reviewResponseList = reviewService.getMyReviews(userDetails.getUsername(), pageable);

        return ResponseEntity.ok(APIResponse.success("Your reviews fetched successfully", reviewResponseList));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<APIResponse<ReviewResponse>> deleteReview(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.deleteReview(id, userDetails.getUsername());

        return ResponseEntity.ok(APIResponse.success("Review deleted successfully"));
    }
}
