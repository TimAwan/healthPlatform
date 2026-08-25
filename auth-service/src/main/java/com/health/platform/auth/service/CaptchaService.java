package com.health.platform.auth.service;

import com.health.platform.core.constant.RedisKeyConstants;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.result.CommonErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
public class CaptchaService {

    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom random = new SecureRandom();

    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public com.health.platform.auth.dto.CaptchaVO generate() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String text = randomText();
        redisTemplate.opsForValue().set(RedisKeyConstants.CAPTCHA + captchaId, text, TTL);
        return new com.health.platform.auth.dto.CaptchaVO(captchaId, base64Image(text));
    }

    /**
     * 校验并立即失效（一次性使用），满足说明书 30 章验证码过期与使用次数限制要求。
     */
    public void verify(String captchaId, String input) {
        String key = RedisKeyConstants.CAPTCHA + captchaId;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null || !stored.equalsIgnoreCase(input)) {
            throw new BizException(CommonErrorCode.CAPTCHA_ERROR);
        }
        redisTemplate.delete(key);
    }

    private String randomText() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return builder.toString();
    }

    private String base64Image(String text) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            graphics.setColor(new Color(220, 220, 220));
            for (int i = 0; i < 6; i++) {
                graphics.setStroke(new BasicStroke(1f));
                graphics.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                        random.nextInt(WIDTH), random.nextInt(HEIGHT));
            }
            graphics.setFont(new Font("Arial", Font.BOLD, 24));
            for (int i = 0; i < text.length(); i++) {
                graphics.setColor(new Color(20 + random.nextInt(100), 20 + random.nextInt(100), 20 + random.nextInt(100)));
                graphics.drawString(String.valueOf(text.charAt(i)), 18 + i * 24, 28 + random.nextInt(6) - 3);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception e) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR);
        } finally {
            graphics.dispose();
        }
    }
}
