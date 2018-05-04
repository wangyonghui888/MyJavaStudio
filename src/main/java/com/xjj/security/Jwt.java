package com.xjj.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by XuJijun on 2018-05-03.
 */
public class Jwt {
    private static HmacUtils hmacSha256 = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, "hmacKey");
    private static AesUtils aesUtils = new AesUtils("AES/ECB/PKCS5Padding", "aesKey");
    private static ObjectMapper objectMapper = new ObjectMapper();


    public static String generateJwt(JwtPayload payload) throws JsonProcessingException {
        // 拼装和加密 payload:
        //String payload = String.valueOf(type).concat(".").concat(String.valueOf(id));
        String payloadStr = objectMapper.writeValueAsString(payload);
        String aesPayload = aesUtils.encrypt(payloadStr);
        //System.out.println(aesPayload);

        // HMAC-SHA256 签名:
        String hmac = hmacSha256.hmacHex(aesPayload);
        //System.out.println(hmac);

        // 拼装jwt：
        String jwt = aesPayload.concat(".").concat(hmac);
        System.out.println("jwt" + jwt);

        return jwt;
    }


    public static Optional<JwtPayload> extractPayload(String jwt) throws IOException {
        String[] ss = jwt.split("\\.");
        if(ss.length < 2){
            return Optional.empty();
        }

        String aesPayload = ss[0];
        String hmac = ss[1];

        if(StringUtils.isAnyBlank(aesPayload, hmac)){
            return Optional.empty();
        }

        if(!hmac.equals(hmacSha256.hmacHex(aesPayload))){ //签名不符
            return Optional.empty();
        }

        String payload = aesUtils.decrypt(aesPayload);
        System.out.println("decoded payload: " + payload);

        return Optional.ofNullable(objectMapper.readValue(payload, JwtPayload.class));

    }

    public static class JwtPayload{
        private int type;
        private Object id;

        public JwtPayload(){}

        public JwtPayload(int type, Object id) {
            this.type = type;
            this.id = id;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }
    }


    public static void main(String[] args) throws Exception {
        JwtPayload jwtPayload = new JwtPayload(1, "xu");

        String jwt = generateJwt(jwtPayload);

        Optional<JwtPayload> decodedPayload = extractPayload(jwt);
        if(decodedPayload.isPresent()){
            System.out.println(objectMapper.writeValueAsString(decodedPayload.get()));
        }
//        decodedPayload.ifPresent(e -> System.out.println(objectMapper.writeValueAsString(e)));


    }
}
