package com.techacademy.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 一覧画面
    @GetMapping
    public String list(Report report, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        Employee emp = userDetail.getEmployee(); // ログインユーザーのEmployeeを取得
        if(Employee.Role.ADMIN == emp.getRole())  { // ログインユーザーのRoleが管理者か？
        model.addAttribute("listSize", reportService.findAll().size());
        model.addAttribute("reportList", reportService.findAll()); // 管理者の場合、全件取得
        } else {
            report.setEmployee(userDetail.getEmployee()); //自身の日報のみ取得
            model.addAttribute("listSize", reportService.findByEmployee(emp).size());
            model.addAttribute("report", report);

            // model.addAttribute("reportList", report.getReportDate()); //
        }

        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable Integer id, Model model) {

        model.addAttribute("report", reportService.findById(id));
        return "reports/detail";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(Report report, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        // report=new Report(); //
        report.setEmployee(userDetail.getEmployee());
        model.addAttribute("report", report);

        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail,
            Model model) {
        report.setEmployee(userDetail.getEmployee());
        model.addAttribute("report", report);

        // 日付重複チェック ★ココを用確認
     //   if ("reportDate".equals(report.getReportDate())) {
       //     // 登録しようとしている日付けがすでにある場合
       //     model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR),
       //             ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
     //       return create(report, userDetail, model);
     //   }

        // 入力チェック
        if (res.hasErrors()) {
            System.out.println(report.getReportDate());
            return create(report, userDetail, model);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = reportService.save(report, userDetail); // 元はsave(report);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(report, userDetail, model);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR), // 重複チェックエラー(例外あり)
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR)); // 重複チェックエラー(例外あり)
            return create(report, userDetail, model);
        }

        System.out.println("ooete5");
        return "redirect:/reports";

    }

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = reportService.delete(id, userDetail);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findById(id));
            return detail(id, model);
        }

        return "redirect:/reports";
    }

    // <追記>日報更新画面の表示
    @GetMapping(value = "/{id}/update")
    public String update(@PathVariable("id") Integer id, Report report, LocalDate reportDate,
            @AuthenticationPrincipal UserDetail userDetail, Model model) {
        // ↑getUserから書き換え
        // Modelに登録,idがnullか否かをifで分ける
        if (id == null) {
            model.addAttribute("report");
        }
        if (id != null) {
            model.addAttribute("report", reportService.findById(id));
        }

        // 日報更新画面に遷移
        return "reports/update";
    }

    // <追記２>日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@Validated Report report, LocalDate reportDate, BindingResult res,
            @PathVariable("id") Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) { // 引数idを追加
        //// 2月23日追記
        System.out.println("kakunin1");
        report.setEmployee(userDetail.getEmployee());
        model.addAttribute("report", report);

     // 日付重複チェック ★ココを用確認
      //  if ("LocalDate".equals(report.getReportDate())) { //localDate型に
            // 登録しようとしている日付けがすでにある場合
        //    model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR),
          //          ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
          // update(id, report, reportDate, userDetail, model);
     //   }

        if (res.hasErrors()) {
            // System.out.println("kakunin2");
            ErrorKinds result = reportService.saveReport(report, userDetail);
        }

        try {
            ErrorKinds result = reportService.saveReport(report, userDetail);

            if (ErrorMessage.contains(result)) {
                System.out.println("Error name=" + ErrorMessage.getErrorName(result) + "[" +ErrorMessage.getErrorValue(result)+ "]");
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return update(null, report, reportDate, userDetail, model);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            System.out.println(report.getReportDate());
            return update(id, report, reportDate, userDetail, model);

            // id = null; // idにnullを設定 //
            // return "reports/update"; // return getUser(code, model);から書き換え
        }
        // 日報更新情報登録
        // Report newReport = reportService.findById(id);
        // newReport.setReportDate(report.getReportDate());
        // newReport.setTitle(report.getTitle());
        // newReport.setContent(report.getContent());
        reportService.saveReport(report, userDetail); // 保存 元はsave(newReport)
        // 一覧画面にリダイレクト
        return "redirect:/reports"; // 更新→一覧への遷移
    }

}