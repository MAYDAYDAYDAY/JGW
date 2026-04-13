package com.example.utils;

import java.net.URLEncoder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.entity.ChatData;
import com.example.entity.Message;
import com.google.gson.Gson;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.util.Base64;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

public class BigModelNew extends WebSocketListener {
    // 地址与鉴权信息  https://spark-api.xf-yun.com/v1.1/chat   1.5地址  domain参数为general
    // 地址与鉴权信息  https://spark-api.xf-yun.com/v2.1/chat   2.0地址  domain参数为generalv2
//    public static final String hostUrl = "https://spark-api.xf-yun.com/v1.1/chat";
//    public static final String appid = "be14c571";
//    public static final String apiSecret = "ZDdlZDg3ZTE1Y2JmNzM1YzE1NmNiM2U4";
//    public static final String apiKey = "2654d117b2a4f5378fa67de2d2f2f766";

    public static final String createHostUrl = "https://cn-huadong-1.xf-yun.com/v1/private/s3fd61810/create";
    public static final String queryHostUrl = "https://cn-huadong-1.xf-yun.com/v1/private/s3fd61810/query";

    /** 讯飞图生图要求宽、高均大于 224，这里统一到不小于该值，避免 10909 */
    private static final int SPARK_IMAGE_MIN_EDGE = 256;

    public static final String hostUrl = "https://spark-api.xf-yun.com/v4.0/chat";
    public static final String domain = "4.0Ultra";
    public static final String appid = "13443803";
    public static final String apiSecret = "ZGYzMDAwNzI0MTZlZThjMjYyYTNjM2Fh";
    public static final String apiKey = "221d5eb3474092d1a96151fdf9590188";

    private static final ConcurrentHashMap<String, Object> QUERY_TASK_LOCKS = new ConcurrentHashMap<>();

    public static List<RoleContent> historyList=new ArrayList<>(); // 对话历史存储集合

    public static String totalAnswer=""; // 大模型的答案汇总

    public static  String NewQuestion = "";
    public static final Gson gson = new Gson();

    // 个性化参数
    private String userId;
    private Boolean wsCloseFlag;

    private static Boolean totalFlag=true; // 控制提示用户是否输入
    /*==============================*/
    private final CountDownLatch latch; // 添加 CountDownLatch

    public  static ChatData chatData = new ChatData();

    private  Message modelAnswer ;

    public Message getModelAnswer() {
        return modelAnswer;
    }
    // 构造函数
    public BigModelNew(String userId, Boolean wsCloseFlag, int count) {
        this.modelAnswer = new Message();
        this.userId = userId;
        this.wsCloseFlag = wsCloseFlag;
        latch = new CountDownLatch(count);
    }

    // 主函数
    public static Message Start(ChatData _chatData,Message newmessage) throws Exception {
        chatData = _chatData;
        BigModelNew util = null;
        if(totalFlag){

            totalFlag=false;
            NewQuestion=newmessage.getContent();
            // 构建鉴权url
            String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
            System.out.println("====authUrl："+authUrl);
            OkHttpClient client = new OkHttpClient.Builder().build();
            String url = authUrl.toString().replace("http://", "ws://").replace("https://", "wss://");
            System.out.println("====Url："+url);
            Request request = new Request.Builder().url(url).build();

            int i = chatData.getSession().getSessionID();

            totalAnswer="";
            util = new BigModelNew(i + "", false,1);
//            WebSocket webSocket = client.newWebSocket(request, new BigModelNew(i + "",false,1));
            WebSocket webSocket = client.newWebSocket(request, util);
            util.latch.await();
            System.out.println("*****"+util.getModelAnswer().getContent());
        }else{
            Thread.sleep(200);
        }
        return util.getModelAnswer();
    }

    public void setModelAnswer(Message modelAnswer) {
        this.modelAnswer = modelAnswer;
    }

    public String FormatTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    public static String staticFormatTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }


    public static boolean canAddHistory(){  // 由于历史记录最大上线1.2W左右，需要判断是能能加入历史
        int history_length=0;
        for(RoleContent temp:historyList){
            history_length=history_length+temp.content.length();
        }
        if(history_length>12000){
            historyList.remove(0);
            historyList.remove(1);
            historyList.remove(2);
            historyList.remove(3);
            historyList.remove(4);
            return false;
        }else{
            return true;
        }
    }


    // 线程来发送音频与参数
    class MyThread extends Thread {
        private WebSocket webSocket;

        public MyThread(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        public void run() {
            try {
                JSONObject requestJson=new JSONObject();

                JSONObject header=new JSONObject();  // header参数
                header.put("app_id",appid);
                header.put("uid",UUID.randomUUID().toString().substring(0, 10));

                JSONObject parameter=new JSONObject(); // parameter参数
                JSONObject chat=new JSONObject();
                chat.put("domain",domain);
                chat.put("temperature",0.5);
                chat.put("max_tokens",4096);
                parameter.put("chat",chat);

                JSONObject payload=new JSONObject(); // payload参数
                JSONObject message=new JSONObject();
                JSONArray text=new JSONArray();

                RoleContent rolesystem = new RoleContent();
                rolesystem.role = "system";
                rolesystem.content = "你是一个甲骨文方面的小助手，你负责为用户解答,（解答需要清晰明了，内容简洁）（回答必须是一整段话，不要分条，分段，不要有序号）（每次回复，不要多于100字）";
                text.add(JSON.toJSON(rolesystem));

                if (historyList.size() > 0) {
                    for (RoleContent tempRoleContent : historyList) {
                        if ("system".equals(tempRoleContent.role)) {
                            continue;
                        }
                        text.add(JSON.toJSON(tempRoleContent));
                    }
                }
                RoleContent roleContent = new RoleContent();
                roleContent.role = "user";
                roleContent.content = NewQuestion;
                text.add(JSON.toJSON(roleContent));
                historyList.add(roleContent);


                message.put("text",text);
                payload.put("message",message);


                requestJson.put("header",header);
                requestJson.put("parameter",parameter);
                requestJson.put("payload",payload);
                // System.err.println(requestJson); // 可以打印看每次的传参明细
                webSocket.send(requestJson.toString());
                // 等待服务端返回完毕后关闭
                while (true) {
                    // System.err.println(wsCloseFlag + "---");
                    Thread.sleep(200);
                    if (wsCloseFlag) {
                        break;
                    }
                }
                webSocket.close(1000, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        System.out.print("大模型：");
        MyThread myThread = new MyThread(webSocket);
        myThread.start();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        // System.out.println(userId + "用来区分那个用户的结果" + text);
        JsonParse myJsonParse = gson.fromJson(text, JsonParse.class);
        System.out.println(myJsonParse);
        if (myJsonParse.header.code != 0) {
            System.out.println("发生错误，错误码为：" + myJsonParse.header.code);
            System.out.println("本次请求的sid为：" + myJsonParse.header.sid);
            webSocket.close(1000, "");
        }
        List<Text> textList = myJsonParse.payload.choices.text;
        for (Text temp : textList) {
            System.out.print("TEXT" + temp.content);
            totalAnswer=totalAnswer+temp.content;
        }

        if(myJsonParse.header.status == 2) {
            System.out.println();
            System.out.println("******BigModelNew*******************************************************************************");
            modelAnswer.setWho(0);
            modelAnswer.setContent(totalAnswer);
            modelAnswer.setTimePoint(FormatTime());

            latch.countDown();
            System.out.println("modelAnswer" + modelAnswer.getContent());

            wsCloseFlag = true;
            totalFlag = true;
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        try {
            if (null != response) {
                int code = response.code();
                System.out.println("onFailure code:" + code);
                System.out.println("onFailure body:" + response.body().string());
                if (101 != code) {
                    System.out.println("connection failed");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // 鉴权方法
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // System.err.println(sha);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        // System.err.println(httpUrl.toString());
        return httpUrl.toString();
    }

    //返回的json结果拆解
    class JsonParse {
        Header header;
        Payload payload;
    }

    class Header {
        int code;
        int status;
        String sid;
    }

    class Payload {
        Choices choices;
    }

    class Choices {
        List<Text> text;
    }

    class Text {
        String role;
        String content;
    }
    class RoleContent{
        String role;
        String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }



    // 新增方法：构建创建任务请求数据
    public static  JSONObject genCreateRequestData(String bText){
        JSONObject data = new JSONObject();
        JSONObject header = new JSONObject();
        header.put("app_id", appid);
        header.put("status", 3);
        header.put("channel", "default");
        header.put("callback_url", "default");

        JSONObject parameter = new JSONObject();
        JSONObject oig = new JSONObject();
        JSONObject result = new JSONObject();
        result.put("encoding", "utf8");
        result.put("compress", "raw");
        result.put("format", "json");
        oig.put("result", result);
        parameter.put("oig", oig);

        JSONObject payload = new JSONObject();
        JSONObject oigPayload = new JSONObject();
        oigPayload.put("text", bText);
        payload.put("oig", oigPayload);

        data.put("header", header);
        data.put("parameter", parameter);
        data.put("payload", payload);

        return data;

    }

    /**
     * 将过小图片按比例放大，使宽、高均不小于 minEdge（讯飞报错：Invalid image size, must more than 224）。
     */
    private static String ensureSparkImageMinEdge(String imageBase64, int minEdge) throws Exception {
        if (imageBase64 == null || imageBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("图片为空");
        }
        String b64 = imageBase64.trim();
        int comma = b64.indexOf(',');
        if (comma >= 0 && b64.regionMatches(true, 0, "data:", 0, 5)) {
            b64 = b64.substring(comma + 1);
        }
        b64 = b64.replaceAll("\\s+", "");
        int mod = b64.length() % 4;
        if (mod != 0) {
            b64 = b64 + "====".substring(0, 4 - mod);
        }
        byte[] bytes = Base64.getMimeDecoder().decode(b64);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        if (img == null) {
            throw new IllegalArgumentException("无法解析图片数据");
        }
        int w = img.getWidth();
        int h = img.getHeight();
        if (w >= minEdge && h >= minEdge) {
            return b64;
        }
        double scale = Math.max((double) minEdge / w, (double) minEdge / h);
        int nw = Math.max(minEdge, (int) Math.ceil(w * scale));
        int nh = Math.max(minEdge, (int) Math.ceil(h * scale));
        BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, nw, nh);
            g.drawImage(img, 0, 0, nw, nh, null);
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(scaled, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static String createTask(String imageBase64, String prompt) throws Exception{
        imageBase64 = ensureSparkImageMinEdge(imageBase64, SPARK_IMAGE_MIN_EDGE);
        JSONObject text = new JSONObject();
        JSONArray imageArray = new JSONArray();
        imageArray.add(imageBase64);
        text.put("image", imageArray);
        text.put("prompt", prompt);
        text.put("aspect_ratio", "1:1");
        text.put("negative_prompt", "");
        text.put("img_count", 4);
        text.put("resolution", "2k");

        String bText = Base64.getEncoder().encodeToString(text.toString().getBytes(StandardCharsets.UTF_8));

        String requestUrl = createUrl(createHostUrl);
        JSONObject data = genCreateRequestData(bText);

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, data.toString());
        System.out.println("createTask:");
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("host", new URL(createHostUrl).getHost())
                .addHeader("app_id", appid)
                .build();

        Response response = client.newCall(request).execute();
        String responseText = response.body().string();
        System.out.println("创建任务返回的消息：\n" + responseText);

        JSONObject resp = JSON.parseObject(responseText);
        return resp.getJSONObject("header").getString("task_id");
    }

//    public static JSONObject queryTask(String taskID) throws Exception{
//        JSONObject data = new JSONObject();
//        JSONObject header = new JSONObject();
//        header.put("app_id", appid);
//        header.put("task_id", taskID);
//        data.put("header", header);
//
//        String requestUrl = createUrl(queryHostUrl);
//
//        OkHttpClient client = new OkHttpClient();
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, data.toString());
//
//        Request request = new Request.Builder()
//                .url(requestUrl)
//                .post(body)
//                .addHeader("content-type", "application/json")
//                .addHeader("host", new URL(queryHostUrl).getHost())
//                .addHeader("app_id", appid)
//                .build();
//
//        Response response = client.newCall(request).execute();
//        String responseText = response.body().string();
//        System.out.println("****************************查询任务进度返回的消息************"+'\n'+responseText+'\n');
//        return JSON.parseObject(responseText);
//
//    }
    public static JSONObject queryTask(String taskID) throws Exception {
        Object lock = QUERY_TASK_LOCKS.computeIfAbsent(taskID, k -> new Object());
        synchronized (lock) {
            try {
                System.out.println("[queryTask-v2] taskId=" + taskID);
                return queryTaskBody(taskID);
            } finally {
                QUERY_TASK_LOCKS.remove(taskID, lock);
            }
        }
    }

    /**
     * 单次调用讯飞 query，带有限次重试（仅针对网络故障与 5xx），避免一次 HTTP 请求内死循环轮询导致网关/客户端超时与并发 query。
     */
    private static String callSparkImageQueryOnceWithRetries(OkHttpClient client, String taskID, int maxAttempts)
            throws Exception {
        Exception lastNet = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                JSONObject data = new JSONObject();
                JSONObject header = new JSONObject();
                header.put("app_id", appid);
                header.put("task_id", taskID);
                data.put("header", header);

                String requestUrl = createUrl(queryHostUrl);
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, data.toString());
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("host", new URL(queryHostUrl).getHost())
                        .addHeader("app_id", appid)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    ResponseBody respBody = response.body();
                    String responseText = respBody != null ? respBody.string() : null;
                    if (!response.isSuccessful()) {
                        int httpCode = response.code();
                        System.err.println("查询任务 HTTP " + httpCode + " 响应体: " + responseText);
                        if (responseText != null && responseText.trim().startsWith("{")) {
                            try {
                                JSONObject ej = JSON.parseObject(responseText);
                                if (ej.containsKey("header")) {
                                    int biz = ej.getJSONObject("header").getIntValue("code");
                                    if (biz != 0) {
                                        String msg = ej.getJSONObject("header").getString("message");
                                        throw new Exception("讯飞生图: " + msg + " (code=" + biz + ")");
                                    }
                                }
                            } catch (Exception bizEx) {
                                if (bizEx.getMessage() != null && bizEx.getMessage().startsWith("讯飞生图:")) {
                                    throw bizEx;
                                }
                            }
                        }
                        if ((httpCode == 429 || httpCode >= 500) && attempt < maxAttempts) {
                            Thread.sleep(2500L * attempt);
                            continue;
                        }
                        throw new Exception("查询任务失败 HTTP " + httpCode + " body: " + responseText);
                    }
                    return responseText;
                }
            } catch (IOException e) {
                lastNet = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(2500L * attempt);
                    continue;
                }
                throw new Exception("查询任务网络异常", e);
            }
        }
        throw new Exception("查询任务失败", lastNet);
    }

    private static JSONObject queryTaskBody(String taskID) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        String responseText = callSparkImageQueryOnceWithRetries(client, taskID, 6);
        System.out.println("****************************查询任务(单次轮询)************" + '\n' + responseText + '\n');

        JSONObject res = JSON.parseObject(responseText);
        int code = res.getJSONObject("header").getIntValue("code");
        String task_status = "";
        if (code != 0) {
            System.out.println(res);
            return res;
        }
        task_status = res.getJSONObject("header").getString("task_status");
        boolean doneByPayload = false;
        if (res.containsKey("payload") && res.get("payload") != null) {
            JSONObject pl = res.getJSONObject("payload");
            if (pl.containsKey("result") && pl.getJSONObject("result").containsKey("text")) {
                String txt = pl.getJSONObject("result").getString("text");
                doneByPayload = txt != null && !txt.isEmpty();
            }
        }
        if ("".equals(task_status)) {
            System.out.println("查询任务状态有误，请检查");
            return res;
        }
        if ("3".equals(task_status) || "4".equals(task_status)
                || ("2".equals(task_status) && doneByPayload)) {
            System.out.println(new java.util.Date());
            System.out.println("任务完成");
            System.out.println(res);
            String f_text = res.getJSONObject("payload").getJSONObject("result").getString("text");
            System.out.println("图片信息：" + f_text);

            String decodedUrl = new String(Base64.getDecoder().decode(f_text));
            System.out.println("解压后的消息:" + decodedUrl);

            JSONArray jsonArray = JSON.parseArray(decodedUrl);
            Set<String> uniqueKeys = new HashSet<>();
            JSONArray uniqueArray = new JSONArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                String subTaskId = item.getString("sub_task_id");
                if (!uniqueKeys.contains(subTaskId)) {
                    uniqueKeys.add(subTaskId);
                    uniqueArray.add(item);
                }
            }

            JSONObject resultObj = new JSONObject();
            if (uniqueArray.size() > 0) {
                JSONObject firstItem = uniqueArray.getJSONObject(0);
                resultObj.put("image_url", firstItem);
            }
            resultObj.put("task_status", task_status);
            resultObj.put("code", 200);
            resultObj.put("waiting", false);
            return resultObj;
        }

        System.out.println("查询任务进行中，等待前端再次轮询……" + res.toJSONString());
        JSONObject pending = new JSONObject();
        pending.put("waiting", true);
        pending.put("task_status", task_status);
        pending.put("code", 200);
        return pending;
    }
    // 新增方法：创建请求URL
    public static String createUrl(String url) throws Exception {
        URL parsedUrl = new URL(url);
        String host = parsedUrl.getHost();
        String path = parsedUrl.getPath();

        // 生成RFC1123格式的时间戳
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        // 拼接字符串
        String signature_origin = "host: " + host + "\n" +
                "date: " + date + "\n" +
                "POST " + path + " HTTP/1.1";

        // 进行hmac - sha256进行加密
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(spec);
        byte[] signatureSha = mac.doFinal(signature_origin.getBytes(StandardCharsets.UTF_8));

        String signatureShaBase64 = Base64.getEncoder().encodeToString(signatureSha);

        String authorization_origin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", signatureShaBase64);

        String authorization = Base64.getEncoder().encodeToString(authorization_origin.getBytes(StandardCharsets.UTF_8));

        // 将请求的鉴权参数组合为字典
        Map<String, String> v = new HashMap<>();
        v.put("authorization", authorization);
        v.put("date", date);
        v.put("host", host);
        // 使用 StringJoiner 拼接鉴权参数
        StringJoiner queryString = new StringJoiner("&");
        for (Map.Entry<String, String> entry : v.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.name());
            queryString.add(key + "=" + encodedValue);
        }
        return url + "?" + queryString.toString();
    }

    // 新增方法：将图片转换为Base64编码字符串
    private static String base64EncodeImage(String filePath) throws IOException {
        File file = new File(filePath);
        try(FileInputStream imageInFile = new FileInputStream(file)){
            byte[] imageData = new byte[(int) file.length()];
            imageInFile.read(imageData);
            return Base64.getEncoder().encodeToString(imageData);
        }
    }
}

