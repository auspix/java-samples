// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// [START gmail_quickstart]

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;

public class GmailReader {
	/**
	 * Application name.
	 */
	private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
	/**
	 * Global instance of the JSON factory.
	 */
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	/**
	 * Directory to store authorization tokens for this application.
	 */
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	
	private static final String USER = "me";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_MODIFY);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	/**
	 * Creates an authorized Credential object.
	 *
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = GmailReader.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(USER);
		// returns an authorized Credential object.
		return credential;
	}

	public static void main(String... args) throws Exception {
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

//		private static final String USER = "me";
		List<Message> mesageList = service.users().messages().list(USER).setQ("is:unread").execute().getMessages();
		if(mesageList==null) {
			System.out.println("No new messages!");
			return;
		}
		for(Message message:mesageList) {
	        String subject = "";
	        String from = "";
	        String date = "";
	        Message fullMessage = service.users().messages().get(USER, message.getId()).execute();
            List<MessagePartHeader> headers = fullMessage.getPayload().getHeaders();
            for (MessagePartHeader header : headers) {
                switch (header.getName()) {
	                case "Subject":
	                    subject = header.getValue();
	                    break;
	                case "From":
	                    from = header.getValue();
	                    break;
                    case "Date":
                        date = header.getValue();
                        break;
                }
            }
            System.out.println("Subject: " + subject);
            System.out.println("From: " + from);
            System.out.println("Date: " + date);
            markAsRead(service,USER,message.getId());
            System.out.println();
		}
	}
	
    private static void markAsRead(Gmail service, String userId, String messageId) throws IOException {
        ModifyMessageRequest mods = new ModifyMessageRequest().setRemoveLabelIds(Collections.singletonList("UNREAD"));
        service.users().messages().modify(userId, messageId, mods).execute();
    }


}
