// 
// Decompiled by Procyon v0.5.36
// 

package main;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import org.json.JSONArray;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.KeyManager;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import java.util.Scanner;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.io.FileNotFoundException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import org.json.JSONObject;
import java.util.ArrayList;

public class main
{
    public static String apiUsername;
    public static String apiPID;
    public static String apiPort;
    public static String apiPassword;
    public static String apiProtocol;
    public static String username;
    public static String tag;
    public static String puuid;
    public static String region;
    public static int accountLevel;
    public static boolean inGame;
    public static int latestRankTier;
    public static String currentRank;
    public static String aToken;
    public static String eToken;
    public static String appDataPath;
    public static String lockFileContents;
    public static boolean grabbedMatch;
    public static String matchId;
    public static ArrayList<JSONObject> playerList;
    public static ArrayList<String> playerPUUIDS;
    public static ArrayList<String> localChatPuuids;
    public static ArrayList<String> userList;
    public static String clientVersion;
    public static boolean firstTimeRun;
    private static TrustManager[] trustAllCerts;
    
    static {
        main.apiUsername = "";
        main.apiPID = "";
        main.apiPort = "";
        main.apiPassword = "";
        main.apiProtocol = "";
        main.username = "";
        main.tag = "";
        main.puuid = "";
        main.region = "";
        main.inGame = false;
        main.currentRank = "";
        main.aToken = "";
        main.eToken = "";
        main.appDataPath = "";
        main.lockFileContents = "";
        main.grabbedMatch = false;
        main.matchId = "";
        main.playerList = new ArrayList<JSONObject>();
        main.playerPUUIDS = new ArrayList<String>();
        main.localChatPuuids = new ArrayList<String>();
        main.userList = new ArrayList<String>();
        main.clientVersion = "";
        main.firstTimeRun = true;
        main.trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                
                @Override
                public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                }
                
                @Override
                public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                }
            } };
    }
    
    public static void main(final String[] args) throws FileNotFoundException, InterruptedException {
        start();
    }
    
    public static void start() throws InterruptedException, FileNotFoundException {
        if (gameOpen()) {
            if (main.firstTimeRun) {
                sortLockFile();
                getUserInfo();
                getEntitlementToken();
                getRegion();
                getAccountLevel();
                getClientVersion();
                System.out.println("Welcome " + main.username + "#" + main.tag + " | Region : " + main.region + " | Account Level : " + main.accountLevel + " | Current Rank : " + sortMMR(getMMR(main.puuid)));
                System.out.println("");
                main.firstTimeRun = false;
            }
            getCurrentStatus();
            String currentStatus = "Not In Game";
            while (!main.inGame) {
                getCurrentStatus();
                TimeUnit.SECONDS.sleep(5L);
            }
            if (main.inGame) {
                currentStatus = "In Game";
            }
            System.out.println("                                 Found New Game");
            System.out.println("                 Waiting For Agent Selection Then Grabbing Data");
            while (!main.grabbedMatch) {
                getCurrentMatchId();
                TimeUnit.SECONDS.sleep(5L);
            }
            if (main.grabbedMatch) {
                main.grabbedMatch = false;
                main.playerList.clear();
                main.playerPUUIDS.clear();
                main.localChatPuuids.clear();
                main.userList.clear();
                System.out.println("");
                getMatchPlayers();
                getLocalChatPuuids();
                getPlayersPUUIDS();
                constructPlayerInfo();
                makeItPretty();
                waitForNextMatch();
            }
        }
        else {
            System.out.println("Open Val first ya spacca");
        }
    }
    
    public static void sortLockFile() {
        final String[] split = main.lockFileContents.split(":");
        main.apiUsername = split[0];
        main.apiPID = split[1];
        main.apiPort = split[2];
        main.apiPassword = split[3];
        main.apiProtocol = split[4];
    }
    
    public static boolean gameOpen() throws FileNotFoundException {
        final String OS = System.getProperty("os.name").toUpperCase();
        String workingDirectory;
        if (OS.contains("WIN")) {
            workingDirectory = System.getenv("AppData");
        }
        else {
            workingDirectory = System.getProperty("user.home");
            workingDirectory = String.valueOf(workingDirectory) + "/Library/Application Support";
        }
        main.appDataPath = workingDirectory.substring(0, workingDirectory.length() - 8);
        final String path2 = "\\Local\\Riot Games\\Riot Client\\Config\\lockfile";
        final String finalPath = String.valueOf(main.appDataPath) + path2;
        if (new File(finalPath).isFile()) {
            final File lockfile = new File(finalPath);
            final Scanner scanner = new Scanner(lockfile);
            main.lockFileContents = scanner.nextLine();
            scanner.close();
            return true;
        }
        return false;
    }
    
    public static void getUserInfo() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final String basicAuth = Base64.getEncoder().encodeToString(("riot:" + main.apiPassword).getBytes(StandardCharsets.UTF_8));
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://127.0.0.1:" + main.apiPort + "/chat/v1/session")).header("Authorization", "Basic " + basicAuth).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            main.username = obj.getString("game_name");
            main.tag = obj.getString("game_tag");
            main.puuid = obj.getString("puuid");
        }
        catch (IOException e) {
            System.out.println("getUserInfo IO Exception");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such AlgorithmException");
        }
    }
    
    public static void getEntitlementToken() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final String basicAuth = Base64.getEncoder().encodeToString(("riot:" + main.apiPassword).getBytes(StandardCharsets.UTF_8));
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://127.0.0.1:" + main.apiPort + "/entitlements/v1/token")).header("Authorization", "Basic " + basicAuth).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            main.aToken = obj.getString("accessToken");
            main.eToken = obj.getString("token");
        }
        catch (IOException e) {
            System.out.println("ent token io");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such AlgorithmException");
        }
    }
    
    public static void getRegion() throws FileNotFoundException {
        final String path2 = "\\Local\\VALORANT\\Saved\\Logs\\ShooterGame.log";
        final String finalPath = String.valueOf(main.appDataPath) + path2;
        String regionLine = "";
        if (new File(finalPath).isFile()) {
            final File shootergamelog = new File(finalPath);
            final Scanner scanner = new Scanner(shootergamelog);
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                if (line.contains("LogPlatformCommon: Platform HTTP Query End. QueryName: [Config_FetchConfig]")) {
                    regionLine = line;
                    break;
                }
            }
            final String regionTrim = regionLine.substring(regionLine.indexOf("config") + 7);
            regionTrim.trim();
            final String[] split = regionTrim.split("]");
            main.region = split[0];
            scanner.close();
        }
    }
    
    public static void getAccountLevel() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://pd." + main.region + ".a.pvp.net/account-xp/v1/players/" + main.puuid)).header("X-Riot-Entitlements-JWT", main.eToken).header("Authorization", "Bearer " + main.aToken).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            main.accountLevel = obj.getJSONObject("Progress").getInt("Level");
        }
        catch (IOException e) {
            System.out.println("account level io");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such AlgorithmException");
        }
    }
    
    public static void getCurrentMatchId() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://glz-" + main.region + "-1." + main.region + ".a.pvp.net/core-game/v1/players/" + main.puuid)).header("X-Riot-Entitlements-JWT", main.eToken).header("Authorization", "Bearer " + main.aToken).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            if (obj.has("MatchID")) {
                main.grabbedMatch = true;
                main.matchId = obj.getString("MatchID");
            }
            else {
                main.grabbedMatch = false;
            }
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such Algorithm Exception");
        }
    }
    
    public static void getCurrentStatus() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final String basicAuth = Base64.getEncoder().encodeToString(("riot:" + main.apiPassword).getBytes(StandardCharsets.UTF_8));
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://127.0.0.1:" + main.apiPort + "/chat/v4/presences")).header("Authorization", "Basic " + basicAuth).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            final JSONArray arr = obj.getJSONArray("presences");
            for (int i = 0; i < arr.length(); ++i) {
                final String userName = arr.getJSONObject(i).getString("game_name");
                final String userTag = arr.getJSONObject(i).getString("game_tag");
                if (userName.equals(main.username) && userTag.equals(main.tag)) {
                    final String current = arr.getJSONObject(i).getString("state");
                    if (current.equals("dnd")) {
                        main.inGame = true;
                    }
                    else {
                        main.inGame = false;
                    }
                }
            }
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such Algorithm Exception");
        }
    }
    
    public static void getClientVersion() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://glz-" + main.region + "-1." + main.region + ".a.pvp.net/session/v1/sessions/" + main.puuid)).header("X-Riot-Entitlements-JWT", main.eToken).header("Authorization", "Bearer " + main.aToken).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            main.clientVersion = obj.getString("clientVersion");
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such Algorithm Exception");
        }
    }
    
    public static int getMMR(final String playerpuuid) {
        int intMMR = 0;
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://pd." + main.region + ".a.pvp.net/mmr/v1/players/" + playerpuuid)).header("X-Riot-Entitlements-JWT", main.eToken).header("Authorization", "Bearer " + main.aToken).header("X-Riot-ClientVersion", main.clientVersion).header("X-Riot-ClientPlatform", "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp9").method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            final String currentSeason = obj.getJSONObject("LatestCompetitiveUpdate").getString("SeasonID");
            intMMR = obj.getJSONObject("QueueSkills").getJSONObject("competitive").getJSONObject("SeasonalInfoBySeasonID").getJSONObject(currentSeason).getInt("CompetitiveTier");
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such Algorithm Exception");
        }
        return intMMR;
    }
    
    public static String sortMMR(final int currentMMR) {
        String textRank = "";
        if (currentMMR == 24) {
            textRank = "Radiant";
        }
        else if (currentMMR == 23) {
            textRank = "Immortal 3";
        }
        else if (currentMMR == 22) {
            textRank = "Immortal 2";
        }
        else if (currentMMR == 21) {
            textRank = "Immortal 1";
        }
        else if (currentMMR == 20) {
            textRank = "Diamond 3";
        }
        else if (currentMMR == 19) {
            textRank = "Diamond 2";
        }
        else if (currentMMR == 18) {
            textRank = "Diamond 1";
        }
        else if (currentMMR == 17) {
            textRank = "Platinum 3";
        }
        else if (currentMMR == 16) {
            textRank = "Platinum 2";
        }
        else if (currentMMR == 15) {
            textRank = "Platinum 1";
        }
        else if (currentMMR == 14) {
            textRank = "Gold 3";
        }
        else if (currentMMR == 13) {
            textRank = "Gold 2";
        }
        else if (currentMMR == 12) {
            textRank = "Gold 1";
        }
        else if (currentMMR == 11) {
            textRank = "Silver 3";
        }
        else if (currentMMR == 10) {
            textRank = "Silver 2";
        }
        else if (currentMMR == 9) {
            textRank = "Silver 1";
        }
        else if (currentMMR == 8) {
            textRank = "Bronze 3";
        }
        else if (currentMMR == 7) {
            textRank = "Bronze 2";
        }
        else if (currentMMR == 6) {
            textRank = "Bronze 1";
        }
        else if (currentMMR == 5) {
            textRank = "Iron 3";
        }
        else if (currentMMR == 4) {
            textRank = "Iron 2";
        }
        else if (currentMMR == 3) {
            textRank = "Iron 1";
        }
        else if (currentMMR == 0) {
            textRank = "broken";
        }
        return textRank;
    }
    
    public static void getMatchPlayers() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://glz-" + main.region + "-1." + main.region + ".a.pvp.net/core-game/v1/matches/" + main.matchId)).header("X-Riot-Entitlements-JWT", main.eToken).header("Authorization", "Bearer " + main.aToken).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            final JSONArray playerArray = obj.getJSONArray("Players");
            for (int i = 0; i < playerArray.length(); ++i) {
                main.playerList.add(playerArray.getJSONObject(i));
            }
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such Algorithm Exception");
        }
    }
    
    public static void getPlayerLoadout() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://pd." + main.region + ".a.pvp.net/personalization/v2/players/" + main.puuid + "/playerloadout")).header("X-Riot-Entitlements-JWT", main.eToken).header("Authorization", "Bearer " + main.aToken).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject jsonObject = new JSONObject(response.body());
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such AlgorithmException");
        }
    }
    
    public static void getLocalChatPuuids() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final String basicAuth = Base64.getEncoder().encodeToString(("riot:" + main.apiPassword).getBytes(StandardCharsets.UTF_8));
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://127.0.0.1:" + main.apiPort + "/chat/v5/participants/")).header("Authorization", "Basic " + basicAuth).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            final JSONArray playerArray = obj.getJSONArray("participants");
            for (int i = 0; i < playerArray.length(); ++i) {
                main.localChatPuuids.add(playerArray.getJSONObject(i).getString("puuid"));
            }
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such AlgorithmException");
        }
    }
    
    public static void getPlayersPUUIDS() {
        for (int i = 0; i < main.playerList.size(); ++i) {
            main.playerPUUIDS.add(main.playerList.get(i).getJSONObject("PlayerIdentity").getString("Subject"));
        }
    }
    
    public static String getAgent(final String agentID) {
        String agentName = "";
        if (agentID.equals("add6443a-41bd-e414-f6ad-e58d267f4e95")) {
            agentName = "Jett";
        }
        else if (agentID.equals("dade69b4-4f5a-8528-247b-219e5a1facd6")) {
            agentName = "Fade";
        }
        else if (agentID.equals("22697a3d-45bf-8dd7-4fec-84a9e28c69d7")) {
            agentName = "Chamber";
        }
        else if (agentID.equals("117ed9e3-49f3-6512-3ccf-0cada7e3823b")) {
            agentName = "Cypher";
        }
        else if (agentID.equals("320b2a48-4d9b-a075-30f1-1f93a9b638fa")) {
            agentName = "Sova";
        }
        else if (agentID.equals("bb2a4828-46eb-8cd1-e765-15848195d751")) {
            agentName = "Neon";
        }
        else if (agentID.equals("1e58de9c-4950-5125-93e9-a0aee9f98746")) {
            agentName = "Killjoy";
        }
        else if (agentID.equals("f94c3b30-42be-e959-889c-5aa313dba261")) {
            agentName = "Raze";
        }
        else if (agentID.equals("707eab51-4836-f488-046a-cda6bf494859")) {
            agentName = "Viper";
        }
        else if (agentID.equals("a3bfb853-43b2-7238-a4f1-ad90e9e46bcc")) {
            agentName = "Reyna";
        }
        else if (agentID.equals("8e253930-4c05-31dd-1b6c-968525494517")) {
            agentName = "Omen";
        }
        else if (agentID.equals("7f94d92c-4234-0a36-9646-3a87eb8b5c89")) {
            agentName = "Yoru";
        }
        else if (agentID.equals("9f0d8ba9-4140-b941-57d3-a7ad57c6b417")) {
            agentName = "Brimstone";
        }
        else if (agentID.equals("6f2a04ca-43e0-be17-7f36-b3908627744d")) {
            agentName = "Skye";
        }
        else if (agentID.equals("41fb69c1-4189-7b37-f117-bcaf1e96f1bf")) {
            agentName = "Astra";
        }
        else if (agentID.equals("601dbbe7-43ce-be57-2a40-4abd24953621")) {
            agentName = "KAY/O";
        }
        else if (agentID.equals("569fdd95-4d10-43ab-ca70-79becc718b46")) {
            agentName = "Sage";
        }
        else if (agentID.equals("5f8d3a7f-467b-97f3-062c-13acf203c006")) {
            agentName = "Breach";
        }
        else if (agentID.equals("eb93336a-449b-9c1b-0a54-a891f7921d69")) {
            agentName = "Phoenix";
        }
        else {
            agentName = "New Agent ?";
        }
        return agentName;
    }
    
    public static void constructPlayerInfo() {
        main.localChatPuuids.retainAll(main.playerPUUIDS);
        final Set<String> set = new HashSet<String>(main.localChatPuuids);
        main.localChatPuuids.clear();
        main.localChatPuuids.addAll(set);
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, main.trustAllCerts, new SecureRandom());
            final String basicAuth = Base64.getEncoder().encodeToString(("riot:" + main.apiPassword).getBytes(StandardCharsets.UTF_8));
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://127.0.0.1:" + main.apiPort + "/chat/v5/participants/")).header("Authorization", "Basic " + basicAuth).method("GET", HttpRequest.BodyPublishers.noBody()).build();
            final HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final JSONObject obj = new JSONObject(response.body());
            final JSONArray playerArray = obj.getJSONArray("participants");
            for (int i = 0; i < playerArray.length(); ++i) {
                final String currentUUID = playerArray.getJSONObject(i).getString("puuid");
                if (main.localChatPuuids.contains(currentUUID)) {
                    final String tempUser = playerArray.getJSONObject(i).getString("game_name");
                    final String tempTag = playerArray.getJSONObject(i).getString("game_tag");
                    final String tempUserTag = String.valueOf(tempUser) + "#" + tempTag;
                    final String tempUserRank = sortMMR(getMMR(currentUUID));
                    String tempTeamID = "";
                    String tempCharacterID = "";
                    int tempAccountLevel = 0;
                    final HttpRequest request2 = HttpRequest.newBuilder().uri(URI.create("https://glz-" + main.region + "-1." + main.region + ".a.pvp.net/core-game/v1/matches/" + main.matchId)).header("X-Riot-Entitlements-JWT", main.eToken).header("Authorization", "Bearer " + main.aToken).method("GET", HttpRequest.BodyPublishers.noBody()).build();
                    final HttpClient client2 = HttpClient.newBuilder().sslContext(sslContext).build();
                    final HttpResponse<String> response2 = client2.send(request2, HttpResponse.BodyHandlers.ofString());
                    final JSONObject obj2 = new JSONObject(response2.body());
                    final JSONArray playerArray2 = obj2.getJSONArray("Players");
                    for (int o = 0; o < playerArray2.length(); ++o) {
                        final String compareUUID = playerArray2.getJSONObject(o).getString("Subject");
                        if (compareUUID.equals(currentUUID)) {
                            tempTeamID = playerArray2.getJSONObject(o).getString("TeamID");
                            final String tempAgentID = playerArray2.getJSONObject(o).getString("CharacterID");
                            tempCharacterID = getAgent(tempAgentID);
                            tempAccountLevel = playerArray2.getJSONObject(o).getJSONObject("PlayerIdentity").getInt("AccountLevel");
                        }
                    }
                    final String tempMaster = String.valueOf(tempUserTag) + ":" + tempAccountLevel + ":" + tempUserRank + ":" + tempTeamID + ":" + tempCharacterID;
                    main.userList.add(tempMaster);
                }
            }
            final Set<String> usernameSet = new HashSet<String>(main.userList);
            main.userList.clear();
            main.userList.addAll(usernameSet);
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
        catch (InterruptedException e2) {
            System.out.println("Interrupted Excception");
        }
        catch (KeyManagementException e3) {
            System.out.println("Key Management Exception");
        }
        catch (NoSuchAlgorithmException e4) {
            System.out.println("No Such AlgorithmException");
        }
    }
    
    public static void makeItPretty() {
        final ArrayList<String> redTeam = new ArrayList<String>();
        final ArrayList<String> blueTeam = new ArrayList<String>();
        for (int i = 0; i < main.userList.size(); ++i) {
            final String currentString = main.userList.get(i);
            final String[] split = currentString.split(":");
            final String usernameString = split[0];
            final String tempAccountLevel = split[1];
            final String tempRank = split[2];
            final String tempTeam = split[3];
            final String tempAgentID = split[4];
            final String tempMasterString = "| Agent : " + tempAgentID + " | Username : " + usernameString + " | Rank : " + tempRank + " | Account Level : " + tempAccountLevel + " |";
            if (tempTeam.equals("Red")) {
                redTeam.add(tempMasterString);
            }
            else if (tempTeam.equals("Blue")) {
                blueTeam.add(tempMasterString);
            }
        }
        System.out.println("------------------------------------------------------------------------------------");
        System.out.println("                                     Red Team                                       ");
        System.out.println("------------------------------------------------------------------------------------");
        for (int i = 0; i < redTeam.size(); ++i) {
            System.out.println(redTeam.get(i));
        }
        System.out.println("------------------------------------------------------------------------------------");
        System.out.println("                                    Blue Team                                       ");
        System.out.println("------------------------------------------------------------------------------------");
        for (int i = 0; i < blueTeam.size(); ++i) {
            System.out.println(blueTeam.get(i));
        }
        System.out.println("------------------------------------------------------------------------------------");
    }
    
    public static void waitForNextMatch() throws InterruptedException, FileNotFoundException {
        while (main.inGame) {
            TimeUnit.SECONDS.sleep(5L);
            getCurrentStatus();
        }
        start();
    }
}
