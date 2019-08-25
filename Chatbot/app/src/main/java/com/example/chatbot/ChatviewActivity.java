package com.example.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatviewActivity extends AppCompatActivity {

    private ChatView chatView;
    private IChatUser user, bot;
    private final static String TAG = ChatviewActivity.class.getName();
    final String auth = "ya29.c.ElpvBzqdo3gkqtB9Aq6yMwNChTm4UnjDnyHVV2HXFmcm0hX-ANv3eItjfLEfENa_Nm_T_q5XAd_ZgheIZBMSwap-YUn7kQnVDMPk8xyaa3GvMV61R6OGpQNb4TI";
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
                Message sendMessage = null;
                try {
                    sendMessage = createUserMessage(null);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
        firestore.collection("chat")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                Messages messages = document.toObject(Messages.class);
                                Message sendMessage, replyMessage;
                                try {
                                    sendMessage = createUserMessage(messages);
                                    replyMessage = createBotMessage(messages, null);

                                    chatView.send(sendMessage);
                                    chatView.setInputText("");

                                    chatView.receive(replyMessage);


                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                            }
                        }
                    }
                });

    }

    private Message createUserMessage(Messages messages) throws ParseException {
        Message message = new Message.Builder()
                .setUser(user)
                .setRight(true)
                .hideIcon(true)
                .build();
        if(messages == null) {
            message.setText(chatView.getInputText());
        } else {
            message.setText(messages.getUserMessage());
            SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            Date date = format.parse(messages.getSendTime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            message.setSendTime(calendar);
        }

        return message;
    }

    private Message createBotMessage(Messages messages, String reply) throws ParseException {
        Message message = new Message.Builder()
                .setUser(bot)
                .setRight(false)
                .hideIcon(true)
                .build();
        if(messages == null) {
            message.setText(reply);
        } else {
            message.setText(messages.getBotReply());
            SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            Date date = format.parse(messages.getReplyTime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            message.setSendTime(calendar);
        }

        return message;
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

                                Message recMessage = null;
                                try {
                                    recMessage = createBotMessage(null, reply);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
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
