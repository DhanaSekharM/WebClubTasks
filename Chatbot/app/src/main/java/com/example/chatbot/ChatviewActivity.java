package com.example.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.bassaer.chatmessageview.model.IChatUser;
import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatviewActivity extends AppCompatActivity {

    private ChatView chatView;
    private IChatUser user, bot;
    private final static String TAG = ChatviewActivity.class.getName();
    final String auth = "ya29.c.ElpvB8inhM5iIt9YGg25BrLqYdxGMga5w2iOUFEihNOZbBW2paCE4morVS0QN1-nWz4iF9TdAMqY_GeuaxKGa0M2KHsbKHCUobM3oBEH2vKlT1e1VpjA25Q2N_4";
    String url = "https://dialogflow.googleapis.com/v2/projects/chatbot-hetaqa/agent/sessions/123456789:detectIntent";
    private final static String userId = "0", botId = "1";
    private final static String userName = "User", botName = "Bot";
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatview);
        chatView = findViewById(R.id.chat_view);

        user = createUser(userId, userName);
        bot = createUser(botId, botName);

        setChatViewAttributes();

        populateChatviewFromDb();

        chatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message sendMessage = new Message.Builder()
                        .setUser(user)
                        .setRight(true)
                        .setText(chatView.getInputText())
                        .hideIcon(true)
                        .build();
                try {
                    makeRequest(sendMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                chatView.send(sendMessage);
                chatView.setInputText("");

            }
        });

    }

    private void populateChatviewFromDb() {
      

    }

    /**
     * Make a post request to the dialogflow api to display the bot reply
     *
     * @param sendMessage user inputted message object
     */
    private void makeRequest(final Message sendMessage) throws JSONException {

        JSONObject body = createJsonRequestObject(sendMessage.getText());

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, url, body,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                String reply = "";
                                try {
                                    reply = response.getJSONObject("queryResult").getString("fulfillmentText");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Message recMessage = new Message.Builder()
                                        .setUser(bot)
                                        .setRight(false)
                                        .setText(reply)
                                        .hideIcon(true)
                                        .build();
                                chatView.receive(recMessage);
                                SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                                String sendTime = format.format(sendMessage.getSendTime().getTime());
                                String recvTime = format.format(recMessage.getSendTime().getTime());
                                Messages chat = new Messages(sendMessage.getText(), reply, sendTime, recvTime);
                                Log.d(TAG, recMessage.getDateSeparateText());

                                //store in firebase db

//                                String id = firestore.collection("chat").document().getId();
                                firestore.collection("chat")
                                        .add(chat)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "Error adding document", e);
                                            }
                                        });
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(ChatviewActivity.this, error.toString(), Toast.LENGTH_LONG).show();

                            }
                        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                header.put("Authorization", "Bearer " + auth);

                return header;
            }
        };
        Volley.newRequestQueue(this)
                .add(request)
                .setTag(TAG);
    }

    /**
     * creates the Json request body with the required format
     *
     * @param inputText
     * @return final Json
     */
    private JSONObject createJsonRequestObject(String inputText) throws JSONException {
        Map<String, String> text = new HashMap<>();
        JSONObject textObject = new JSONObject();
        JSONObject requestObject = new JSONObject();

        text.put("text", inputText);
        text.put("language_code", "en-US");
        JSONObject jsonObject = new JSONObject(text);
        textObject.put("text", jsonObject);
        requestObject.put("query_input", textObject);

        return requestObject;

    }

    private void setChatViewAttributes() {
        chatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        chatView.setLeftBubbleColor(Color.WHITE);
        chatView.setBackgroundColor(ContextCompat.getColor(this, R.color.lightBlue));
        chatView.setRightMessageTextColor(Color.BLACK);
        chatView.setLeftMessageTextColor(Color.BLACK);
        chatView.setInputTextHint("");
        chatView.setMessageMarginTop(5);
        chatView.setMessageMarginBottom(5);
    }

    private IChatUser createUser(final String id, final String name) {
        return new IChatUser() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Bitmap getIcon() {
                return null;
            }

            @Override
            public void setIcon(Bitmap bitmap) {

            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Volley.newRequestQueue(this).cancelAll(TAG);
    }
}
