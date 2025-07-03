package com.music.application.be.modules.forget_password;

import com.music.application.be.modules.forget_password.dto.ChangePassword;
import com.music.application.be.modules.forget_password.dto.MailBody;
import com.music.application.be.modules.user.User;
import com.music.application.be.modules.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@Service
public class ForgetPasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ForgetPasswordRepository forgetPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    public ForgetPasswordService(UserRepository userRepository, EmailService emailService,
                                 ForgetPasswordRepository forgetPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.forgetPasswordRepository = forgetPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<String> verifyEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email does not exist"));

        int otp = generateOtp();
        MailBody mailBody = MailBody.builder()
                .to(email)
                .text("This is the OTP for your forgot password: " + otp)
                .subject("OTP for request forgot password")
                .build();

        ForgetPassword fp = forgetPasswordRepository.findByUser(user)
                .map(existing -> {
                    existing.setOtp(otp);
                    existing.setExpirationTime(new Date(System.currentTimeMillis() + 70 * 1000));
                    return existing;
                })
                .orElseGet(() -> ForgetPassword.builder()
                        .otp(otp)
                        .expirationTime(new Date(System.currentTimeMillis() + 70 * 1000))
                        .user(user)
                        .build());

        emailService.sendSimpleEmail(mailBody);
        forgetPasswordRepository.save(fp);

        return ResponseEntity.ok("Email sent for verification");
    }

    public ResponseEntity<String> verifyOtp(Integer otp, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        ForgetPassword fp = forgetPasswordRepository.findByUserAndOtp(otp, user)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid OTP for email: " + email));

        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgetPasswordRepository.delete(fp);
            return new ResponseEntity<>("OTP expired", HttpStatus.EXPECTATION_FAILED);
        }

        return ResponseEntity.ok("OTP verified");
    }

    public ResponseEntity<String> changePassword(ChangePassword changePassword, String email) {
        if (!changePassword.password().equals(changePassword.repeatPassword())) {
            return new ResponseEntity<>("Please enter the password again", HttpStatus.EXPECTATION_FAILED);
        }

        String encodedPassword = passwordEncoder.encode(changePassword.password());
        userRepository.updatePassword(email, encodedPassword);

        return ResponseEntity.ok("Password changed successfully");
    }

    private Integer generateOtp() {
        Random rand = new Random();
        return rand.nextInt(100_000, 999_999);
    }
}
