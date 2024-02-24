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
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;
    // private final EmployeeService employeeService; //追記
    private Integer id;

    @Autowired
    public ReportService(ReportRepository reportRepository, PasswordEncoder passwordEncoder) {
        this.reportRepository = reportRepository;
        this.passwordEncoder = passwordEncoder;
        // this.employeeService = employeeService; //追記

    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report, UserDetail userDetail) { // 自分の従業員情報をとってくる土台

        // 日付重複チェック
        List<Report> reportList = reportRepository.findByEmployeeAndReportDate(userDetail.getEmployee(),
                report.getReportDate()); // 自分のログインした情報だけの従業員情報を取得　findByEmployeeAndReportDate(Employee employee, LocalDate reportdate);
        if (reportList.size() != 0) { // 日付が同じだった場合エラー sizeは件数を取得する
            return ErrorKinds.DUPLICATE_ERROR; // ←DATECHECK_ERROR？「すでに登録された日付」と出したい。
        }

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

//   private Object findByEmployeeAndReportDate(boolean equals) { //修正済?
//        return null;
//    }

//private List<Report> existsByEmployee(Employee employee) {  //repositoryクラスと結びづける
//   return reportRepository.existsByEmployee(employee);
//    }

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

    /** 日報の更新を行なう */
    @Transactional
    public ErrorKinds saveReport(Report report, UserDetail userDetail) {
        // if 新しく登録する日付がもともと画面にある日付けと同じ。OK。 →登録可能
        // 新しく登録する日付けが別のデータを保存している日付け。NGの可能性。他にあるか要チェック
        Report oldReport = findById(report.getId());
        System.out.println("その１");

      // if (report.getReportDate().isEqual(oldReport.getReportDate())) {
            if (report.getReportDate().toString().equals(oldReport.getReportDate().toString())) {
            System.out.println("その２");
        } else {
            // 次の処理。id/日付重複チェック
            List<Report> reportList = reportRepository.findByEmployeeAndReportDate(userDetail.getEmployee(),
                    report.getReportDate()); // 自分のログインした情報だけの従業員情報を取得 //この処理が走ってない
            System.out.println("その３" + reportList.size());
            if ((reportList.size() != 0)) { // idが同じ かつ 同じ日がある。 同じ日付がある場合はエラー ※表示されている日は例外でOKはifで処理
                return ErrorKinds.DUPLICATE_ERROR;
            }
        }

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        // report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);

        return ErrorKinds.SUCCESS;

    }
}

/**
 * 日報の更新を行なう ・修正前
 *
 * @Transactional public Report saveReport(Report report) { return
 *                reportRepository.save(report); }
 */
