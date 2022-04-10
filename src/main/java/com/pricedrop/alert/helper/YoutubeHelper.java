package com.pricedrop.alert.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class YoutubeHelper {

//    private static final String CLIENT_SECRETS= "client_secret.json";
//    private static final Collection<String> SCOPES =
//            Collections.singletonList("https://www.googleapis.com/auth/youtube.upload");
//
//    private static final String APPLICATION_NAME = "PriceDrop Alert";
//    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//
//    @Value("${output.path}")
//    private String outputPath;
//
//    @Value("${input.resource.path}")
//    private String inputPath;
//
//    private Credential authorize(final NetHttpTransport httpTransport) throws IOException {
//        // Load client secrets.
//        InputStream inputStream = Main.class.getResourceAsStream("/" + CLIENT_SECRETS);
//        assert inputStream != null;
//        GoogleClientSecrets clientSecrets =
//                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));
//        // Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow =
//                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
//                        .build();
//        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
//    }
//
//    private YouTube getService() throws GeneralSecurityException, IOException {
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        Credential credential = authorize(httpTransport);
//        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    }
//
//    public void uploadVideo() throws IOException, GeneralSecurityException {
//        YouTube youtubeService = getService();
//
//        // Define the Video object, which will be uploaded as the request body.
//        Video video = new Video();
//
//        // Add the snippet object property to the Video object.
//        VideoSnippet snippet = new VideoSnippet();
//        snippet.setCategoryId("22");
//        snippet.setChannelId("UCfH-_BTisFYXFBJkrD1UlAA");//price drop alert channel id
//        snippet.setChannelTitle("Price Drop Alert");
//        snippet.setDescription("Test video desc.");
//        String[] tags = {
//                "Test1",
//                "Test2",
//        };
//        snippet.setTags(Arrays.asList(tags));
//        snippet.setTitle("Test video title.");
//        video.setSnippet(snippet);
//
//        // Add the status object property to the Video object.
//        VideoStatus status = new VideoStatus();
////        status.setEmbeddable(false);
////        status.setMadeForKids(false);
//        status.setPrivacyStatus("private");//make public once all testing is done
//        video.setStatus(status);
//
//        File mediaFile = new File(outputPath + "/output.mp4");
//        InputStreamContent mediaContent =
//                new InputStreamContent("application/octet-stream",
//                        new BufferedInputStream(new FileInputStream(mediaFile)));
//        mediaContent.setLength(mediaFile.length());
//
//        // Define and execute the API request
//        YouTube.Videos.Insert request = youtubeService.videos()
//                .insert(Collections.singletonList("snippet,status"), video, mediaContent);
//        Video response = request.execute();
//        log.info(response.toString());
//    }


//    DEPENDENCIES

//    		<dependency>
//			<groupId>com.google.api-client</groupId>
//			<artifactId>google-api-client</artifactId>
//			<version>1.33.2</version>
//		</dependency>
//		<dependency>
//			<groupId>com.google.apis</groupId>
//			<artifactId>google-api-services-youtube</artifactId>
//			<version>v3-rev20210915-1.32.1</version>
//		</dependency>
//		<dependency>
//			<groupId>com.google.http-client</groupId>
//			<artifactId>google-http-client-jackson</artifactId>
//			<version>1.15.0-rc</version>
//		</dependency>
//		<dependency>
//			<groupId>com.google.oauth-client</groupId>
//			<artifactId>google-oauth-client-java6</artifactId>
//			<version>1.15.0-rc</version>
//		</dependency>
//		<dependency>
//			<groupId>com.google.oauth-client</groupId>
//			<artifactId>google-oauth-client-jetty</artifactId>
//			<version>1.33.1</version>
//		</dependency>
//		<dependency>
//			<groupId>com.google.api-client</groupId>
//			<artifactId>google-api-client-jackson2</artifactId>
//			<version>1.20.0</version>
//		</dependency>
}
