package com.dduru.gildongmu.report.service;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.report.domain.Report;
import com.dduru.gildongmu.report.dto.request.ReportCreateRequest;
import com.dduru.gildongmu.report.dto.response.ReportCreateResponse;
import com.dduru.gildongmu.report.exception.DuplicatePostReportException;
import com.dduru.gildongmu.report.exception.SelfPostReportNotAllowedException;
import com.dduru.gildongmu.report.repository.ReportRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    public ReportCreateResponse create(Long postId, Long userId,ReportCreateRequest request) {
        User user = userRepository.getByIdOrThrow(userId);
        Post post = postRepository.getActiveByIdOrThrow(postId);
        validateReport(post, userId);

        Report report = createReport(user, post, request);
        Report savedReport = reportRepository.save(report);
        return ReportCreateResponse.from(savedReport);

    }

    private Report createReport(User user, Post post, ReportCreateRequest request) {
        return Report.createReport(user, post, request.reason(), request.description());

    }

    private void validateReport(Post post, Long userId) {
        if (post.getUser().getId().equals(userId)) {
            throw new SelfPostReportNotAllowedException();
        }

        if (reportRepository.existsByPostIdAndUserId(post.getId(), userId)) {
            throw new DuplicatePostReportException();
        }
    }
}
