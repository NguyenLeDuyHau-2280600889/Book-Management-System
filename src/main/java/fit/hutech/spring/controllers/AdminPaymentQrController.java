package fit.hutech.spring.controllers;

import fit.hutech.spring.services.PaymentQrService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/payment-qr")
public class AdminPaymentQrController {
    private final PaymentQrService paymentQrService;

    public AdminPaymentQrController(PaymentQrService paymentQrService) {
        this.paymentQrService = paymentQrService;
    }

    @GetMapping
    public String paymentQrPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "success", required = false) String success,
                                Model model) {
        model.addAttribute("paymentQrImage", paymentQrService.getQrImagePath());
        model.addAttribute("paymentQrNote", paymentQrService.getQrNote());
        model.addAttribute("error", error);
        model.addAttribute("success", success);
        return "admin/payment_qr";
    }

    @PostMapping
    public String updatePaymentQr(@RequestParam(value = "qrFile", required = false) MultipartFile qrFile,
                                  @RequestParam(value = "paymentQrNote", required = false) String paymentQrNote) {
        try {
            paymentQrService.saveQr(qrFile, paymentQrNote);
            return "redirect:/admin/payment-qr?success=Da+cap+nhat+ma+QR";
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin/payment-qr?error=" + encode(ex.getMessage());
        } catch (Exception ex) {
            return "redirect:/admin/payment-qr?error=Khong+the+cap+nhat+ma+QR";
        }
    }

    private String encode(String text) {
        if (text == null || text.isBlank()) {
            return "Khong+hop+le";
        }
        return text.trim().replace(" ", "+");
    }
}
