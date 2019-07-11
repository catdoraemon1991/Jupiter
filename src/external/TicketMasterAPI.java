package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import entity.Item.ItemBuilder;
import entity.*;


public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "AqxXvLdHt3SA5n44NZ14q8M3gQuGdDIY";

	public List<Item> search(double lat, double lon, String keyword) {
		List<Item> itemList = new ArrayList<>();
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s&size=100", API_KEY, geoHash, keyword, 100);
//		String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radius=%s&size=20", API_KEY, lat, lon, keyword, 50);
		String url = URL + "?" + query;
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			
			int responseCode = connection.getResponseCode();
			System.out.println("Sending request to url: " + url);
			System.out.println("Response code: " + responseCode);
			
			if (responseCode != 200) {
				return new ArrayList<Item>();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			StringBuilder response = new StringBuilder();
			
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			JSONObject obj = new JSONObject(response.toString());
			
			if (!obj.isNull("_embedded")) {
				JSONObject embedded = obj.getJSONObject("_embedded");
				JSONArray events = embedded.getJSONArray("events");
				List<TicketMasterObject> TmObjs = new ArrayList<>();
				for (int i = 0; i < events.length(); i++) {
					JSONObject event = events.getJSONObject(i);
					ObjectMapper objectMapper = new ObjectMapper();
				    TicketMasterObject TmObj = objectMapper.readValue(event.toString(), TicketMasterObject.class);
				    TmObjs.add(TmObj);
				}
				return getItemList(TmObjs);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return itemList;
	}
	
	private List<Item> getItemList(List<TicketMasterObject> TmObjs) throws JSONException {
		// build Item object from the TickerMasterObject instance 
		List<Item> itemList = new ArrayList<>();
		for (int i = 0; i < TmObjs.size(); ++i) {
			TicketMasterObject TmObj = TmObjs.get(i);
			
			ItemBuilder builder = new ItemBuilder();
			if (TmObj.getId() != null) {
				builder.setItemId(TmObj.getId());
			}
			if (TmObj.getName() != null) {
				builder.setName(TmObj.getName());
			}
			if (TmObj.getUrl() != null) {
				builder.setUrl(TmObj.getUrl());
			}
			builder.setDistance(TmObj.getDistance());			
			builder.setAddress(getAddress(TmObj));
			builder.setCategories(getCategories(TmObj));
			builder.setImageUrl(getImageUrl(TmObj));
			
			itemList.add(builder.build());

		}
		return itemList;		
	}
	private String getImageUrl(TicketMasterObject TmObj) throws JSONException {
		if (TmObj != null) {
			List<Image> images = TmObj.getImages();
			for (int i = 0; i < images.size(); i++) {
				Image image = images.get(i);
				if (image != null) {
					return image.getUrl();
				}
			}
		}
		return "";
	}

	private Set<String> getCategories(TicketMasterObject TmObj) throws JSONException {		
		Set<String> categories = new HashSet<>();		
		if (TmObj != null) {
			List<Classification> classifications = TmObj.getClassifications();
			for (int i = 0; i < classifications.size(); ++i) {
				Classification classification = classifications.get(i);
				if (classification != null) {
					Segment segment = classification.getSegment();
					if (segment != null) {
						categories.add(segment.getName());
					}
				}
			}
		}
		return categories;
	}

	private String getAddress(TicketMasterObject TmObj) throws JSONException {
		if (TmObj != null) {
			Embedded embedded = TmObj.get_embedded();
			if (embedded != null) {
				List<Venue> venues = embedded.getVenues();
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < venues.size(); ++i) {
					Venue venue = venues.get(i);				
					if (venue != null) {
						Lines address = venue.getAddress();
						if (address != null) {
							if (address.getLine1() != null) {
								builder.append(address.getLine1());
							}
							if (address.getLine2() != null) {
								builder.append(",");
								builder.append(address.getLine2());
							}
							if (address.getLine3() != null) {
								builder.append(",");
								builder.append(address.getLine3());
							}
						}
						if (venue.getCity() != null) {
							City city = venue.getCity();
							if (city.getName() != null) {
								builder.append(",");
								builder.append(city.getName());
							}
						}
						if (venue.getState() != null) {
							State state = venue.getState();
							if (state.getName() != null) {
								builder.append(",");
								builder.append(state.getName());
							}
						}
						if (venue.getPostalCode() != null) {
							builder.append(",");
							builder.append(venue.getPostalCode());
						}
					}
					String result = builder.toString();
					if (!result.isEmpty()) {
						return result;
					}
				}
			}
		}
		return "";
		
	}

	public void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);
		try {
			for (Item item : events) {
				System.out.println(item.toJSONObject());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);

	}

}
