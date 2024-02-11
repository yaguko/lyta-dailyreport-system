package com.techacademy.controller;

import java.time.LocalDate;

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
    public String list(Model model) {

        model.addAttribute("listSize", reportService.findAll().size());
        model.addAttribute("reportList", reportService.findAll());

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
    model.addAttribute("report",report);

        return "reports/new";
        }


    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        report.setEmployee(userDetail.getEmployee());
        model.addAttribute("report",report);

        // 入力チェック
        if (res.hasErrors()) {
            System.out.println("ooete");
            System.out.println(report.getReportDate());
            return create(report, userDetail, model);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = reportService.save(report);

            if (ErrorMessage.contains(result)) {
                System.out.println("ooete2");
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(report, userDetail, model);
            }

        } catch (DataIntegrityViolationException e) {
            System.out.println("ooete3");
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            System.out.println("ooete4");
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
    public String getUser(@PathVariable("id") Integer id, LocalDate reportDate, @AuthenticationPrincipal UserDetail userDetail, Model model) {

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
    public String update(@Validated Report report, BindingResult res, Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) { // 引数idを追加
        report.setEmployee(userDetail.getEmployee());
        model.addAttribute("report",report);

        System.out.println("ooetewe");

        if (res.hasErrors()) {

            System.out.println("ooetewe34");

            // エラーあり
            id = null; // idにnullを設定 //
            return "reports/update"; // return getUser(code, model);から書き換え
        }
        // 日報更新情報登録
        Report newReport = reportService.findById(id);
        newReport.setReportDate(report.getReportDate());
        newReport.setTitle(report.getTitle());
        newReport.setContent(report.getContent());
        reportService.save(newReport);
        // 一覧画面にリダイレクト
        return "redirect:/reports"; // 更新→一覧への遷移
    }

}