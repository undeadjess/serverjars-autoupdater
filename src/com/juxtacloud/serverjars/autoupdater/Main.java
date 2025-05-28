package com.juxtacloud.serverjars.autoupdater;

import java.io.*;
import java.util.*;
import java.net.URI;
import java.net.http.*;

import org.json.*;

public class Main {
    private static final String API_BASE = "https://serverjars.juxtacloud.com/api/";
    private static final String configFilePath = "serverjars.properties";

    public static void main(String[] args) {
        Main main = new Main();
        main.initializeConfig(configFilePath);

        System.out.println("---------------------------------------------------");
        System.out.println("             ServerJars AutoUpdater");
        System.out.println("    Built with love by Jessica S. and Juxtacloud");
        System.out.println("---------------------------------------------------\n");
        
        // get type from config. if not present, get types from API and ask user which one to use
        String type = main.readConfig("type", configFilePath);
        if (type == null || type.isEmpty()) {
            System.out.println("No type found in config. Fetching types from API...");
            String types = main.getTypes();
            if (types != null) {
                System.out.println("Available types: " + types);
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter the type you want to use: ");
                type = scanner.nextLine();
                main.writeConfig("type", type, configFilePath);
            } else {
                System.out.println("Failed to fetch types from API.");
                return;
            }
        }

        // get server from config. if not present, get servers from API and ask user which one to use
        String server = main.readConfig("server", configFilePath);
        if (server == null || server.isEmpty()) {
            System.out.println("No server found in config. Fetching servers from API...");
            String servers = main.getServers(type);
            if (servers != null) {
                System.out.println("Available servers: " + servers);
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter the server you want to use: ");
                server = scanner.nextLine();
                main.writeConfig("server", server, configFilePath);
            } else {
                System.out.println("Failed to fetch servers from API.");
                return;
            }
        }

        // get version from config. if not present, get versions from API and ask user which one to use
        String version = main.readConfig("version", configFilePath);
        if (version == null || version.isEmpty()) {
            System.out.println("No version found in config. Fetching versions from API...");
            String versions = main.getVersions(type, server);
            if (versions != null) {
                System.out.println("Available versions: " + versions);
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter the version you want to use: ");
                version = scanner.nextLine();
                main.writeConfig("version", version, configFilePath);
            } else {
                System.out.println("Failed to fetch versions from API.");
                return;
            }
        }
        // get build from config. if not present, get builds from API and ask user which one to use
        String build = main.readConfig("build", configFilePath);
        if (build == null || build.isEmpty()) {
            System.out.println("No build found in config. Fetching builds from API...");
            String builds = main.getBuilds(type, server, version);
            if (builds != null) {
                System.out.println("Available builds: " + builds);
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter the build you want to use: ");
                build = scanner.nextLine();
                main.writeConfig("build", build, configFilePath);
            } else {
                System.out.println("Failed to fetch builds from API.");
                return;
            }
        }

        System.out.println("---------------------------------------------------");
        System.out.println("Type: " + type);
        System.out.println("Server: " + server);
        System.out.println("Version: " + version);
        System.out.println("Build: " + build);
        System.out.println("fetching from API...");
        String downloadURL = main.getDownloadURL(type, server, version, build);

        if (downloadURL != null || !downloadURL.isEmpty()) {
            System.out.println("Download URL: " + downloadURL);
            // download file
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(downloadURL))
                        .build();
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                InputStream inputStream = response.body();
                FileOutputStream outputStream = new FileOutputStream(server + "-server.jar");
                main.writeConfig("serverjar", server + "-server.jar", configFilePath);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                System.out.println("File downloaded successfully.");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to fetch download URL from API.");
        }
        System.out.println("---------------------------------------------------");
        System.out.println("running server...");
        // run server
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", readConfig("serverjar", configFilePath));
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void initializeConfig(String filePath) {
        // create config file if it doesn't exist
        File configFile = new File(filePath);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                System.out.println("Config file created: " + filePath);

                Properties properties = new Properties();
                properties.setProperty("type", "");
                properties.setProperty("server", "");
                properties.setProperty("version", "");
                properties.setProperty("build", "");
                properties.setProperty("serverjar", "");

                try (OutputStream output = new FileOutputStream(filePath)) {
                    properties.store(output, null);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readConfig(String option, String filePath) {
        // read .properties file
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return properties.getProperty(option);
    }

    public static void writeConfig(String option, String value, String filePath) {
        // write to .properties file
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try (OutputStream output = new FileOutputStream(filePath)) {
            properties.setProperty(option, value);
            properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static String fetchFromAPI(String query) {
        // fetch data from API
        String url = API_BASE + query;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getTypes() {
        // get types from API
        String response = fetchFromAPI("");
        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);
                return json.getString("types");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getServers(String type) {
        // get servers from API
        String response = fetchFromAPI(type);
        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);
                return json.getJSONArray("servers").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getVersions(String type, String server) {
        // get versions from API
        String response = fetchFromAPI(type + "/" + server);
        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);
                return json.getString("versions");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getBuilds(String type, String server, String version) {
        // get builds from API
        String response = fetchFromAPI(type + "/" + server + "/" + version);
        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);
                return json.getString("builds");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getLatest(String type, String server, String version) {
        // get latest from API
        version = (version == null) ? "" : "/" + version;
        String response = fetchFromAPI(type + "/" + server + version);
        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);
                // return json.getJSONObject("latest").toString();
                if (version == "") {
                    // get first version from versions
                    return json.getJSONArray("versions").getString(0);
                } else {
                    // get the first build from builds
                    return json.getJSONArray("builds").getString(0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getDownloadURL(String type, String server, String version, String build) {
        // get download URL from API - remebering build may be null
        build = (build == null) ? "" : "/" + build;
        version = (version == null) ? "" : "/" + version;
        String response = fetchFromAPI(type + "/" + server + version + build);
        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);
                return json.getJSONObject("latest").getString("downloadURL");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
