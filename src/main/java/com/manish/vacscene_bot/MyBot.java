package com.manish.vacscene_bot;

import java.io.FileReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

//import org.json.simple.JSONArray;

//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton.KeyboardButtonBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyBot extends TelegramLongPollingBot {

	private Location locationG;
	// String loc_exact="";
	private String loc_state = "";
	private String loc_district = "";

	public String getBotUsername() {
		// TODO
		return "vaccinator";
	}

	@Override
	public String getBotToken() {
		// TODO
		return "1870209563:AAFPW2-HvCMtaT8Qa-ziUfqN2-QqH4HM-2g";
	}

	public void onUpdateReceived(Update update) {
		// TODO Auto-generated method stub
		// We check if the update has a message and the message has text

		// global variable

		if (update.hasMessage() && update.getMessage().hasText()) {
			SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields

			if (update.hasMessage() && (update.getMessage().getText().equalsIgnoreCase("Hi")
					|| update.getMessage().getText().equalsIgnoreCase("Hello"))) {
				System.out.println(update.getMessage().getFrom().getFirstName());
				System.out.println(update.getMessage().getChatId());
				message.setChatId(update.getMessage().getChatId().toString());
				message.setText(
						"Hello " + update.getMessage().getFrom().getFirstName() + "\n" + "How can I help you today??");

				InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
				List<List<InlineKeyboardButton>> rowsInline = new ArrayList<List<InlineKeyboardButton>>();
				List<InlineKeyboardButton> rowInline1 = new ArrayList<InlineKeyboardButton>();
				// List<InlineKeyboardButton> rowInline = new ArrayList<InlineKeyboardButton>();
				// InlineKeyboardButton b1=new InlineKeyboardButton();

				InlineKeyboardButton b1 = new InlineKeyboardButton();
				b1.setText("FIND SLOTS BY PINCODE");
				b1.setCallbackData("pincode");
				rowInline1.add(b1);

				InlineKeyboardButton b2 = new InlineKeyboardButton();
				b2.setText("SLOTS BY LOCATION");
				b2.setCallbackData("city");
				rowInline1.add(b2);

				// Set the keyboard to the markup
				List<InlineKeyboardButton> rowInline2 = new ArrayList<InlineKeyboardButton>();
				InlineKeyboardButton b11 = new InlineKeyboardButton();
				b11.setText("SHOW CASE STATS");
				b11.setCallbackData("stats");
				rowInline2.add(b11);

				rowsInline.add(rowInline1);
				rowsInline.add(rowInline2);

				// Set the keyboard to the markup
				// rowsInline.add(rowInline);
				// Add it to the message
				markupInline.setKeyboard(rowsInline);
				message.setReplyMarkup(markupInline);

				try {
					execute(message); // Call method to send the message
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}
			} else if (update.hasMessage() && update.getMessage().getText().equalsIgnoreCase("true")) {

				message.setChatId(update.getMessage().getChatId().toString());
				message.setText("We are looking for slots in your location & Nearby!\n" + "Please wait");
				System.out.println(loc_state + " inside true " + loc_district);

				SendMessage messageCallback = new SendMessage();

				long chat_id = update.getMessage().getChatId();
				int dist_id = sendMeDistrictId(loc_district, loc_state);
				// Double latitude= locationG.getLatitude();
				// Double longitude = locationG.getLongitude();

				System.out.println("GOT BACK THE DISTRICT ID WHICH IS" + dist_id);

				try {
					execute(message);

					LocalDate today = LocalDate.now();
					String formattedDate = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
					System.out.println(formattedDate);
					// URL url = new URL("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY");
					URL url = new URL(
							"https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByDistrict?district_id="
									+ dist_id + "&date=" + formattedDate);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("Content-Type", "application/json; utf-8");
					connection.setRequestProperty("Accept", "application/json");

					connection.setRequestMethod("GET");
					connection.connect();
					System.out.println(connection.getResponseCode());
					String inline = "";
					Scanner sc = new Scanner(url.openStream());

					while (sc.hasNext()) {

						inline += sc.next();

					}
					sc.close();

					System.out.println("\nJSON data in string format");
					System.out.println(inline);
					JSONObject obj = new JSONObject(inline);
					JSONArray arr = obj.getJSONArray("sessions");// notice that `"posts": [...]`
					String vac_Hubs = "";
					if (arr.length() == 0) {

						messageCallback.setChatId(Long.toString(chat_id));
						messageCallback.setText(
								"SORRY! We are not able to do a location search at exact location, Try finding slots using the PINCODE.\n "
										+ "Or Try replying NEARBY to check nearby locations as well !!");
						execute(messageCallback);

					} else {
						Boolean slotFound = false;
						for (int i = 0; i < arr.length(); i++) {
							// String post_id = arr.getJSONObject(i).getString("post_id");
							// arr.getJSONObject(i).getString("available_capacity")
							// System.out.println(arr.getJSONObject(i).getInt("available_capacity"));
							vac_Hubs = vac_Hubs + (i + 1) + ")Centre Name: " + arr.getJSONObject(i).getString("name")
									+ "\n";
							if (arr.getJSONObject(i).getInt("available_capacity") != 0) {

								System.out.println(arr.getJSONObject(i));
								slotFound = true;

								messageCallback.setChatId(Long.toString(chat_id));
								messageCallback.setText("**AVAILABLE***\nVaccine: "
										+ arr.getJSONObject(i).getString("vaccine") + "\n" + "Minimum Age Limit: "
										+ arr.getJSONObject(i).getInt("min_age_limit") + "\n" + "Name: "
										+ arr.getJSONObject(i).getString("name") + "\n" + "Address: "
										+ arr.getJSONObject(i).getString("address") + "\n" + "Total Doses available: "
										+ arr.getJSONObject(i).getInt("available_capacity") + "\n"
										+ "1st Dose Available: "
										+ arr.getJSONObject(i).getInt("available_capacity_dose1") + "\n"
										+ "2nd Dose Available: "
										+ arr.getJSONObject(i).getInt("available_capacity_dose2") + "\n"
										+ "Block Name: " + arr.getJSONObject(i).getString("block_name") + "\n");
								execute(messageCallback);

							}

						}
						messageCallback.setChatId(Long.toString(chat_id));
						messageCallback.setText("Want t o check nearby locations as well?? Reply Nearby :)");
						execute(messageCallback);

						if (slotFound == false) {

							messageCallback.setChatId(Long.toString(chat_id));
							messageCallback.setText("SORRY NO SLOTS AVAILABLE IN BELOW VAC-HUB" + "\n" + vac_Hubs
									+ "\n Try replying NEARBY to check nearby locations as well !");
							execute(messageCallback);

						}
					}

				} catch (TelegramApiException e) {
					e.printStackTrace();
				} catch (Exception ex) {
					System.out.println("exception occured");
					ex.printStackTrace();
				}

			} else if (update.hasMessage() && update.getMessage().getText().equalsIgnoreCase("nearby")) {
				message.setChatId(update.getMessage().getChatId().toString());
				message.setText("We are looking for slots in your location & Nearby!\n" + "Please wait");

				SendMessage messageNearby = new SendMessage();

				long chat_id = update.getMessage().getChatId();
				// int dist_id= sendMeDistrictId(loc_district, loc_state);
				Double latitude = locationG.getLatitude();
				Double longitude = locationG.getLongitude();

				System.out.println("Checking nearby location for lat: " + latitude + "and long:" + longitude);

				try {

					execute(message);

					URL urlPin = new URL(
							"https://cdn-api.co-vin.in/api/v2/appointment/centers/public/findByLatLong?lat=" + latitude
							+ "&long=" + longitude);
					HttpURLConnection connection = (HttpURLConnection) urlPin.openConnection();
					connection.setRequestProperty("Content-Type", "application/json; utf-8");
					connection.setRequestProperty("Accept", "application/json");

					connection.setRequestMethod("GET");
					connection.connect();
					System.out.println(connection.getResponseCode());
					String inlinePincodes = "";
					Scanner sc = new Scanner(urlPin.openStream());

					while (sc.hasNext()) {

						inlinePincodes += sc.next();

					}
					sc.close();

					System.out.println("\nJSON data in string format");
					System.out.println(inlinePincodes);
					JSONObject obj = new JSONObject(inlinePincodes);
					JSONArray arrC = obj.getJSONArray("centers");// notice that `"posts": [...]`

					String pincodesNearby = "";

					if (arrC.length() == 0) {

						messageNearby.setChatId(Long.toString(chat_id));
						messageNearby.setText("SORRY! Seems like a problem, try again with specific pincode");
						execute(messageNearby);

					} else {

						message.setText("Here you go! All nearby centers availablity");
						message.setChatId(update.getMessage().getChatId().toString());
						execute(message);
						Boolean slotFound = false;
						String vac_Hubs1 = "";

						LocalDate today = LocalDate.now();
						String formattedDate = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
						System.out.println(arrC);

						Set<String> arr = new HashSet<String>();

						for (int i = 0; i < arrC.length(); i++) {
							arr.add(arrC.getJSONObject(i).getString("pincode"));

						}
						Iterator iter = arr.iterator();
						while (iter.hasNext()) {

							// System.out.println("Fething by pincode" + iter.next());
							//							// URL url = new URL("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY");
							URL url = new URL(
									"https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin?pincode="
											+ iter.next() + "&date=" + formattedDate);

							connection = (HttpURLConnection) url.openConnection();
							connection.setRequestProperty("Content-Type", "application/json; utf-8");
							connection.setRequestProperty("Accept", "application/json");

							connection.setRequestMethod("GET");
							connection.connect();
							System.out.println(connection.getResponseCode());
							String inline = "";
							Scanner sc1 = new Scanner(url.openStream());

							while (sc1.hasNext()) {

								inline += sc1.next();

							}
							sc1.close();

							System.out.println("\nJSON data in string format");
							System.out.println(inline);
							JSONObject obj1 = new JSONObject(inline);
							JSONArray arr1 = obj1.getJSONArray("sessions"); // notice that `"posts": [...]`

							for (int j = 0; j < arr1.length(); j++) {
								// String post_id = arr.getJSONObject(i).getString("post_id");
								// arr.getJSONObject(i).getString("available_capacity")
								// System.out.println(arr.getJSONObject(i).getInt("available_capacity"));

								if (arr1.getJSONObject(j).getInt("available_capacity") != 0) {

									System.out.println(arr1.getJSONObject(j));

									slotFound = true;

									messageNearby.setChatId(Long.toString(chat_id));
									String availableAt = "***AVAILABLE***\nVaccine: "
											+ arr1.getJSONObject(j).getString("vaccine") + "\n" + "Minimum Age Limit: "
											+ arr1.getJSONObject(j).getInt("min_age_limit") + "\n" + "Name: "
											+ arr1.getJSONObject(j).getString("name") + "\n" + "Address: "
											+ arr1.getJSONObject(j).getString("address") + "\n"
											+ "Total Doses available: "
											+ arr1.getJSONObject(j).getInt("available_capacity") + "\n"
											+ "1st Dose Available: "
											+ arr1.getJSONObject(j).getInt("available_capacity_dose1") + "\n"
											+ "2nd Dose Available: "
											+ arr1.getJSONObject(j).getInt("available_capacity_dose2") + "\n"
											+ "Block Name: " + arr1.getJSONObject(j).getString("block_name") + "\n";
									messageNearby.setText(availableAt);
									execute(messageNearby);

								} else {
									vac_Hubs1 = vac_Hubs1 + "\n**" + arr1.getJSONObject(j).getString("name") + "\n "
											+ arr1.getJSONObject(j).getString("address") + ","
											+ arr1.getJSONObject(j).getString("district_name")
											+ "\n---------------------------------------------\n";
								}

							}

						}
						if (slotFound == true && vac_Hubs1 != null) {

							messageNearby.setChatId(Long.toString(chat_id));

							messageNearby.setText("Checked below centres as well,"
									+ "\n NO SLOTS AVAILABLE IN BELOW VAC-HUB" + "\n" + vac_Hubs1);
							execute(messageNearby);

						}

					}
				} catch (TelegramApiException e) {
					e.printStackTrace();
				} catch (Exception ex) {
					System.out.println("exception occured");
					ex.printStackTrace();
				}

			}

			else if (update.hasMessage() && (update.getMessage().getText().toLowerCase().contains("pin:")
					|| (ifPinEntered(update.getMessage().getText())))) {

				System.out.println("pincode detect to h ua hai");
				SendMessage messagePin = new SendMessage();
				long chat_id = update.getMessage().getChatId();
				int pincode;
				if (update.getMessage().getText().toLowerCase().contains("pin:")) {
					String pArr[] = update.getMessage().getText().split(":");
					pincode = Integer.parseInt(pArr[1]);
				} else {
					pincode = Integer.parseInt(update.getMessage().getText());
				}

				messagePin.setChatId(Long.toString(chat_id));
				messagePin.setText("Searching slots for PIN:" + pincode + "\n Please wait!");

				try {
					execute(messagePin);
					// Create a neat value object to hold the URL
					// Function call
					LocalDate today = LocalDate.now();
					String formattedDate = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
					System.out.println(formattedDate);
					// URL url = new URL("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY");
					URL url = new URL("https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin?pincode="
							+ pincode + "&date=" + formattedDate);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("Content-Type", "application/json; utf-8");
					connection.setRequestProperty("Accept", "application/json");

					connection.setRequestMethod("GET");
					connection.connect();
					System.out.println(connection.getResponseCode());
					String inline = "";
					Scanner sc = new Scanner(url.openStream());

					while (sc.hasNext()) {

						inline += sc.next();

					}
					sc.close();

					System.out.println("\nJSON data in string format");
					System.out.println(inline);
					JSONObject obj = new JSONObject(inline);
					JSONArray arr = obj.getJSONArray("sessions"); // notice that `"posts": [...]`
					Boolean slotFound = false;
					String vac_Hubs = "";
					for (int i = 0; i < arr.length(); i++) {
						// String post_id = arr.getJSONObject(i).getString("post_id");
						// arr.getJSONObject(i).getString("available_capacity")
						// System.out.println(arr.getJSONObject(i).getInt("available_capacity"));
						vac_Hubs = vac_Hubs + (i + 1) + ")Centre Name: " + arr.getJSONObject(i).getString("name")
								+ "\n";
						if (arr.getJSONObject(i).getInt("available_capacity") != 0) {

							System.out.println(arr.getJSONObject(i));

							slotFound = true;

							messagePin.setChatId(Long.toString(chat_id));

							messagePin.setText("Vaccine: " + arr.getJSONObject(i).getString("vaccine") + "\n"
									+ "Minimum Age Limit: " + arr.getJSONObject(i).getInt("min_age_limit") + "\n"
									+ "Name: " + arr.getJSONObject(i).getString("name") + "\n" + "Address: "
									+ arr.getJSONObject(i).getString("address") + "\n" + "Total Doses available: "
									+ arr.getJSONObject(i).getInt("available_capacity") + "\n" + "1st Dose Available: "
									+ arr.getJSONObject(i).getInt("available_capacity_dose1") + "\n"
									+ "2nd Dose Available: " + arr.getJSONObject(i).getInt("available_capacity_dose2")
									+ "\n" + "Block Name: " + arr.getJSONObject(i).getString("block_name") + "\n");
							execute(messagePin);

						}

					}

					messagePin.setChatId(Long.toString(chat_id));
					messagePin.setText(
							"Want to check nearby locations as well?? Send LOCATION \n Use the Location button! :)");
					execute(messagePin);

					if (slotFound == false) {

						messagePin.setChatId(Long.toString(chat_id));

						messagePin.setText("SORRY NO SLOTS AVAILABLE IN BELOW VAC-HUB" + "\n" + vac_Hubs);
						execute(messagePin);

					}

				} catch (TelegramApiException e) {
					e.printStackTrace();
				} catch (Exception ex) {
					System.out.println("exception occured");
					ex.printStackTrace();
				}

			}

			else {

				// if (update.hasMessage() &&
				// !(update.getMessage().getText().equalsIgnoreCase("Hi")
				// || update.getMessage().getText().equalsIgnoreCase("Hello")))
				System.out.println("else part" + update.getMessage().getFrom().getFirstName());
				message.setChatId(update.getMessage().getChatId().toString());
				message.setText("Hey " + update.getMessage().getFrom().getFirstName() + "\n"
						+ "Please start with a 'Hi' or a 'Hello' :)");

				try {
					execute(message); // Call method to send the message
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}

			}

		} else if (update.hasMessage() && update.getMessage().hasLocation()) {

			SendMessage messageLocation = new SendMessage();
			long chat_id = update.getMessage().getChatId();
			System.out.println("Location has received");
			Location location = update.getMessage().getLocation();
			this.locationG = location;
			System.out.println(location);

			// https://api.mapbox.com/geocoding/v5/mapbox.places/86.230891,22.80557.json?access_token=pk.eyJ1IjoibWFuaXNobXJpbmFsIiwiYSI6ImNram93cTlodDA5ZnAzMG41ODNqcGdvOHcifQ.xV4er7kVlwm_j2zzkYp4qQ

			try {

				URL url = new URL("https://api.mapbox.com/geocoding/v5/mapbox.places/" + location.getLongitude() + ","
						+ location.getLatitude()
						+ ".json?access_token=pk.eyJ1IjoibWFuaXNobXJpbmFsIiwiYSI6ImNram93cTlodDA5ZnAzMG41ODNqcGdvOHcifQ.xV4er7kVlwm_j2zzkYp4qQ");
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestProperty("Content-Type", "application/json; utf-8");
				connection.setRequestProperty("Accept", "application/json");

				connection.setRequestMethod("GET");
				connection.connect();
				System.out.println(connection.getResponseCode());
				String locDetails = "";
				Scanner sc = new Scanner(url.openStream());

				while (sc.hasNext()) {

					locDetails += sc.next();

				}
				sc.close();

				System.out.println("\nJSON data in string format");
				System.out.println(locDetails);
				JSONObject obj = new JSONObject(locDetails);
				JSONArray arr = obj.getJSONArray("features"); // notice that `"posts": [...]`
				// Boolean slotFound = false;
				String loc_exact = arr.getJSONObject(0).getString("place_name");
				System.out.println("location is :" + loc_exact);

				messageLocation.setChatId(Long.toString(chat_id));
				messageLocation.setText("We have tracked your location. \n" + "Locaton: " + loc_exact + "\n"
						+ "Please confirm if that is correct, reply true");
				String[] loc_split = loc_exact.split(",");
				loc_state = loc_split[loc_split.length - 2];
				loc_district = loc_split[loc_split.length - 3];

				System.out.println("state: " + loc_state + " district: " + loc_district);

				execute(messageLocation);

			} catch (TelegramApiException e) {
				e.printStackTrace();
			} catch (Exception ex) {
				System.out.println("exception occured");
				ex.printStackTrace();
			}

		}

		else if (update.hasCallbackQuery()) {
			// Set variables

			System.out.println("Inside callback");
			String answer = "Updated message text";
			// EditMessageText messageCallback = new EditMessageText();
			SendMessage messageCallback = new SendMessage();
			String call_data = update.getCallbackQuery().getData();
			long message_id = update.getCallbackQuery().getMessage().getMessageId();
			long chat_id = update.getCallbackQuery().getMessage().getChatId();

			System.out.println("Chat instance callback-->" + update.getCallbackQuery().getChatInstance());
			System.out.println("Chat id" + chat_id);

			if (call_data.equals("pincode")) {

				// System.out.println("callback part" +
				// update.getMessage().getFrom().getFirstName());

				messageCallback.setChatId(Long.toString(chat_id));
				messageCallback.setText("Please provide us your pincode.");

				try {
					execute(messageCallback);
					// // Create a neat value object to hold the URL
					// // Function call
					// LocalDate today = LocalDate.now();
					// String formattedDate =
					// today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
					// System.out.println(formattedDate);
					// // URL url = new URL("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY");
					// URL url = new URL(
					// "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByDistrict?district_id=82&date="
					// + formattedDate);
					// HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					// connection.setRequestProperty("Content-Type", "application/json; utf-8");
					// connection.setRequestProperty("Accept", "application/json");
					//
					// connection.setRequestMethod("GET");
					// connection.connect();
					// System.out.println(connection.getResponseCode());
					// String inline = "";
					// Scanner sc = new Scanner(url.openStream());
					//
					// while (sc.hasNext()) {
					//
					// inline += sc.next();
					//
					// }
					// sc.close();
					//
					// System.out.println("\nJSON data in string format");
					// System.out.println(inline);
					// JSONObject obj = new JSONObject(inline);
					// JSONArray arr = obj.getJSONArray("sessions"); // notice that `"posts": [...]`
					// Boolean slotFound = false;
					// for (int i = 0; i < arr.length(); i++) {
					// // String post_id = arr.getJSONObject(i).getString("post_id");
					// // arr.getJSONObject(i).getString("available_capacity")
					// // System.out.println(arr.getJSONObject(i).getInt("available_capacity"));
					//
					// if (arr.getJSONObject(i).getInt("available_capacity") != 0) {
					//
					// System.out.println(arr.getJSONObject(i));
					// slotFound = true;
					//
					// messageCallback.setChatId(Long.toString(chat_id));
					// messageCallback.setText("Vaccine: " +
					// arr.getJSONObject(i).getString("vaccine") + "\n"
					// + "Minimum Age Limit: " + arr.getJSONObject(i).getInt("min_age_limit") + "\n"
					// + "Name: " + arr.getJSONObject(i).getString("name") + "\n" + "Address: "
					// + arr.getJSONObject(i).getString("address") + "\n" + "Total Doses available:
					// "
					// + arr.getJSONObject(i).getInt("available_capacity") + "\n" + "1st Dose
					// Available: "
					// + arr.getJSONObject(i).getInt("available_capacity_dose1") + "\n"
					// + "2nd Dose Available: " +
					// arr.getJSONObject(i).getInt("available_capacity_dose2")
					// + "\n" + "Block Name: " + arr.getJSONObject(i).getString("block_name") +
					// "\n");
					// execute(messageCallback);
					//
					// }
					//
					// }
					//
					// if (slotFound == false) {
					//
					// messageCallback.setChatId(Long.toString(chat_id));
					// messageCallback.setText("SORRY NO SLOTS AVAILABLE");
					// execute(messageCallback);
					//
					// }
					//
				} catch (TelegramApiException e) {
					e.printStackTrace();
				} catch (Exception ex) {
					System.out.println("exception occured");
					ex.printStackTrace();
				}
			} else if (call_data.equals("stats")) {

				// System.out.println("callback part" +
				// update.getMessage().getFrom().getFirstName());

				messageCallback.setChatId(Long.toString(chat_id));
				messageCallback.setText("Please wait while we fetch the details");

				try {
					execute(messageCallback);
					// Create a neat value object to hold the URL

					// URL url = new URL("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY");
					URL url = new URL("https://api.covid19india.org/v4/min/data.min.json");
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("Content-Type", "application/json; utf-8");
					connection.setRequestProperty("Accept", "application/json");

					connection.setRequestMethod("GET");
					connection.connect();
					System.out.println(connection.getResponseCode());
					String inline = "";
					Scanner sc = new Scanner(url.openStream());

					while (sc.hasNext()) {

						inline += sc.next();

					}
					sc.close();
					
					if(loc_district!="" || loc_state!="") {
					System.out.println("\nJSON data in string format");
					// System.out.println(inline);
					System.out.println(loc_state+ "from stats"+loc_district);
					JSONObject obj = new JSONObject(inline);
					
					String code_state=sendStateCodeForStats(loc_state);
					
					JSONObject state = obj.getJSONObject(code_state);
					JSONObject districts = state.getJSONObject("districts");
					JSONObject city = districts.getJSONObject(loc_district);
					JSONObject stats = city.getJSONObject("total");

					System.out.println(stats.toString());

					// for(int i =0;i<obj.length();i++) {}
					messageCallback.setChatId(Long.toString(chat_id));
					messageCallback.setText("--------STATS FOR: "+loc_district+","+loc_state+"---------\n" +
							"Recovered: " + stats.getInt("recovered") + "\n" + "Deceased: " + stats.getInt("deceased")
							+ "\n" + "\n" + "Confirmed Cases: " + stats.getInt("confirmed") + "\n"+"Active cases: "+(stats.getInt("confirmed")-stats.getInt("recovered")-stats.getInt("deceased")));

					// + "Vaccinated: " + stats.getInt("vaccinated")
					execute(messageCallback);
					}
					else {
						messageCallback.setChatId(Long.toString(chat_id));
						messageCallback.setText("Send your location to start with");

						// + "Vaccinated: " + stats.getInt("vaccinated")
						execute(messageCallback);
						
					}
				} catch (TelegramApiException e) {
					e.printStackTrace();
				} catch (Exception ex) {
					System.out.println("exception occured");
					
					ex.printStackTrace();
				}
			}

			else if (call_data.equals("city")) {

				// KeyboardButton kb_loc= new KeyboardButton("location");
				// kb_loc.setRequestLocation(true);
				//
				//

				ReplyKeyboardMarkup kmKeyboardMarkup = new ReplyKeyboardMarkup();
				kmKeyboardMarkup.setResizeKeyboard(true);
				kmKeyboardMarkup.setSelective(true);

				List<KeyboardRow> kblistrow = new ArrayList<KeyboardRow>();

				KeyboardRow kblist = new KeyboardRow();

				KeyboardButton kb = new KeyboardButton();
				kb.setRequestLocation(true);
				kb.setText("Send location");

				// kblist.add(kb);
				kblist.add(kb);
				kblistrow.add(kblist);

				kmKeyboardMarkup.setKeyboard(kblistrow);

				messageCallback.setReplyMarkup(kmKeyboardMarkup);
				messageCallback.setChatId(Long.toString(chat_id));
				messageCallback.setText("Please send location from the button below.");

				try {
					execute(messageCallback);

				} catch (TelegramApiException e) {
					e.printStackTrace();
				} catch (Exception ex) {
					System.out.println("exception occured");
					ex.printStackTrace();
				}

			}

		}

	}

	public int sendMeDistrictId(String district, String state) {
		// https://cdn-api.co-vin.in/api/v2/admin/location/districts/5
		// https://cdn-api.co-vin.in/api/v2/admin/location/states

		int stateId = sendMeStateId(state);
		int districtId = 0;
		try {
			URL url = new URL("https://cdn-api.co-vin.in/api/v2/admin/location/districts/" + stateId);
			// 'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1)
			// AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36'
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Accept", "application/JSON");
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36 Edg/90.0.818.51");
			connection.setRequestProperty("Connection", "keep-alive");
			// connection.setRequestProperty("Accept-Encoding", "");
			connection.setRequestMethod("GET");
			// connection.connect();
			System.out.println("yahaan waala hai" + connection.getResponseCode());
			String inline = "";
			Scanner sc = new Scanner(connection.getInputStream());

			while (sc.hasNext()) {

				inline += sc.next();

			}
			sc.close();

			System.out.println("\nJSON data in string format");
			System.out.println(inline);
			JSONObject obj = new JSONObject(inline);
			JSONArray arr = obj.getJSONArray("districts"); // notice that `"posts": [...]`
			Boolean slotFound = false;
			for (int i = 0; i < arr.length(); i++) {

				if (arr.getJSONObject(i).getString("district_name").equalsIgnoreCase(district)) {

					System.out.println(arr.getJSONObject(i).getInt("district_id"));
					districtId = arr.getJSONObject(i).getInt("district_id");

				}

			}
		} catch (Exception ex) {
			System.out.println("exception occured");
			ex.printStackTrace();
		}

		return districtId;
	}

	public int sendMeStateId(String state) {

		int state_id = 0;

		try {

			URL url = new URL("https://cdn-api.co-vin.in/api/v2/admin/location/states");
			// 'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1)
			// AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36'
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Accept", "application/JSON");
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36 Edg/90.0.818.51");
			connection.setRequestProperty("Connection", "keep-alive");
			// connection.setRequestProperty("Accept-Encoding", "");
			connection.setRequestMethod("GET");
			// connection.connect();
			System.out.println("yahaan waala hai" + connection.getResponseCode());
			String inline = "";
			Scanner sc = new Scanner(connection.getInputStream());

			while (sc.hasNext()) {

				inline += sc.next();

			}
			sc.close();

			System.out.println("\nJSON data in string format");
			System.out.println(inline);
			JSONObject obj = new JSONObject(inline);
			JSONArray arr = obj.getJSONArray("states"); // notice that `"posts": [...]`
			Boolean slotFound = false;
			for (int i = 0; i < arr.length(); i++) {

				if (arr.getJSONObject(i).getString("state_name").equalsIgnoreCase(state)) {

					System.out.println(arr.getJSONObject(i).getInt("state_id"));
					state_id = arr.getJSONObject(i).getInt("state_id");
					break;

				}

			}
		} catch (Exception ex) {
			System.out.println("exception occured");
			ex.printStackTrace();
		}

		return state_id;
	}

	boolean ifPinEntered(String pin) {

		if (pin.matches("[0-9]+") && pin.length() == 6)
			return true;
		else
			return false;

	}
	public static 	String sendStateCodeForStats(String state) {
		
		HashMap<String,String> hmstate= new HashMap<String, String>();
		hmstate.put("Andaman and Nicobar Islands","AN");
		hmstate.put("Andhra Pradesh","AP");
		hmstate.put("Arunachal Pradesh","AR");
		hmstate.put("Assam","AS");
		hmstate.put("Bihar","BR");
		hmstate.put("Chandigarh","CH");
		hmstate.put("Chhattisgarh","CT");
		hmstate.put("Dadra and Nagar Haveli","DN");
		hmstate.put("Daman and Diu","DD");
		hmstate.put("Delhi","DL");
		hmstate.put("Goa","GA");
		hmstate.put("Gujarat","GJ");
		hmstate.put("Haryana","HR");
		hmstate.put("Himachal Pradesh","HP");
		hmstate.put("Jammu and Kashmir","JK");
		hmstate.put("Jharkhand","JH");
		hmstate.put("Karnataka","KA");
		hmstate.put("Kerala","KL");
		hmstate.put("Lakshadweep","LD");
		hmstate.put("Madhya Pradesh","MP");
		hmstate.put("Maharashtra","MH");
		hmstate.put("Manipur","MN");
		hmstate.put("Meghalaya","ML");
		hmstate.put("Mizoram","MZ");
		hmstate.put("Nagaland","NL");
		hmstate.put("Odisha","OR");
		hmstate.put("Puducherry","PY");
		hmstate.put("Punjab","PB");
		hmstate.put("Rajasthan","RJ");
		hmstate.put("Sikkim","SK");
		hmstate.put("Tamil Nadu","TN");
		hmstate.put("Telangana","TG");
		hmstate.put("Tripura","TR");
		hmstate.put("Uttar Pradesh","UP");
		hmstate.put("Uttarakhand","UT");
		hmstate.put("West Bengal","WB");

		return hmstate.get(state);
	}

}
