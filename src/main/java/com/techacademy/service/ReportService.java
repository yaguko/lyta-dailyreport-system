package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ReportService(ReportRepository reportRepository, PasswordEncoder passwordEncoder) {
        this.reportRepository = reportRepository;
        this.passwordEncoder = passwordEncoder;

    }
    

     // 日報保存
    @Transactional
    public ErrorKinds save(Report report) {

        // 日付重複チェック
        if (findByReportDate(report.getReportDate()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    private Object findByReportDate(LocalDate reportDate) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    // 日報削除

    @Transactional
    public ErrorKinds delete(Integer id, UserDetail userDetail) {
        Report report = findById(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }


    // 日報一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 1件を検索
    public Report findById(Integer id) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }

 
}