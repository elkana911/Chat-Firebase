package com.example.eric.chat_firebase.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eric.chat_firebase.ChatMessage;
import com.example.eric.chat_firebase.R;
import com.example.eric.chat_firebase.fcm.FcmNotificationBuilder;
import com.example.eric.chat_firebase.pojo.User;
import com.example.eric.chat_firebase.util.FirebaseConst;
import com.example.eric.chat_firebase.util.SharedPrefUtil;
import com.example.eric.chat_firebase.util.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentChatWith.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentChatWith#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentChatWith extends Fragment {
    private static final String PARAM_SENDER_UID = "senderUid";
    private static final String PARAM_RECEIVER_UID = "receiverUid";
    private static final String PARAM_SENDER_NAME = "senderName";    //buat titie/kirim push notf
    private static final String PARAM_RECEIVER_NAME = "receiverName";
    private static final String PARAM_RECEIVER_FIREBASE_TOKEN = "receiverFBToken";

    // basicnya hanya ada 2 macem room, sender panggil/createroom duluan atau receiver yg duluan
//    final String room_type_1 = senderUid + "_" + receiverUid;
//    final String room_type_2 = receiverUid + "_" + senderUid;

    private String senderUid;
    private String receiverUid;
    private String senderName;
    private String receiverName;
    private String receiverFBToken;

    private String room_type_1;// = senderUid + "_" + receiverUid;
    private String room_type_2;// = receiverUid + "_" + senderUid;


    @BindView(R.id.etMessage)
    EditText etMessage;

    @BindView(R.id.rvMessages)
    RecyclerView rvMessages;

    // meskipun akses ke root dari rooms, listener hanya diperuntukan utk room spy ga perlu listen semua room ?
    private DatabaseReference rooms = FirebaseDatabase.getInstance().getReference().child(FirebaseConst.DB_ROOT_ROOMS);

    private OnFragmentInteractionListener mListener;

    private MoviesAdapter mAdapter;

    public FragmentChatWith() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param senderUid Parameter 1.
     * @return A new instance of fragment FragmentChatWith.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentChatWith newInstance(String senderUid, String senderName, User receiver) {
        FragmentChatWith fragment = new FragmentChatWith();
        Bundle args = new Bundle();
        args.putString(PARAM_SENDER_UID, senderUid);
        args.putString(PARAM_SENDER_NAME, senderName);
        args.putString(PARAM_RECEIVER_UID, receiver.getUid());
        args.putString(PARAM_RECEIVER_NAME, receiver.getName());
        args.putString(PARAM_RECEIVER_FIREBASE_TOKEN, receiver.getFirebaseToken());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            senderUid = getArguments().getString(PARAM_SENDER_UID);
            senderName = getArguments().getString(PARAM_SENDER_NAME);
            receiverUid = getArguments().getString(PARAM_RECEIVER_UID);
            receiverName = getArguments().getString(PARAM_RECEIVER_NAME);
            receiverFBToken = getArguments().getString(PARAM_RECEIVER_FIREBASE_TOKEN);

        }
        room_type_1 = senderUid + "_" + receiverUid;
        room_type_2 = receiverUid + "_" + senderUid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_with, container, false);

        ButterKnife.bind(this, view);

        rvMessages.setLayoutManager(new LinearLayoutManager(getActivity()));

        FloatingActionButton btnSend = (FloatingActionButton) view.findViewById(R.id.fab);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getMessages();
    }

    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();

        final ChatMessage chatMessage = new ChatMessage(senderUid, receiverUid, msg);

        rooms.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(room_type_1)) {
                    rooms.child(room_type_1)
                            .child(String.valueOf(chatMessage.getTimestamp()))
                            .setValue(chatMessage);
                } else if (dataSnapshot.hasChild(room_type_2)) {
                    rooms.child(room_type_2)
                            .child(String.valueOf(chatMessage.getTimestamp()))
                            .setValue(chatMessage);

                } else {
                    // brarti new room
                    rooms.child(room_type_1)
                            .child(String.valueOf(chatMessage.getTimestamp()))
                            .setValue(chatMessage);

                    getMessages();
                }

                //send push notification to receiver
                FcmNotificationBuilder.initialize()
                        .title(senderName)
                        .message(chatMessage.getText())
                        .username(senderName)
                        .uid(senderUid)
                        .firebaseToken(new SharedPrefUtil(getActivity().getApplicationContext()).getString(FirebaseConst.ARG_FIREBASE_TOKEN))
                        .receiverFirebaseToken(receiverFBToken)
                        .send();
                etMessage.setText(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Unable to send message\n" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMessages() {
        rooms.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(room_type_1)) {
                    rooms.child(room_type_1)
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    ChatMessage obj = dataSnapshot.getValue(ChatMessage.class);
                                    addMessageToListView(obj);
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(getActivity(), "Unable to get message\n" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                } else if (dataSnapshot.hasChild(room_type_2)) {
                    rooms.child(room_type_2)
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    ChatMessage obj = dataSnapshot.getValue(ChatMessage.class);

                                    addMessageToListView(obj);
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(getActivity(), "Unable to get message\n" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Log.e("getMessages", "no such room");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Unable to get message.\n" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessageToListView(ChatMessage msg) {
        if (mAdapter == null) {
            mAdapter = new MoviesAdapter(new ArrayList<ChatMessage>());
            rvMessages.setAdapter(mAdapter);
        }
        mAdapter.add(msg);
        rvMessages.smoothScrollToPosition(mAdapter.getItemCount() - 1);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
    }

    private ValueEventListener listenerGetMessagesValueEvent = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot.hasChild(room_type_1)) {

            } else if (dataSnapshot.hasChild(room_type_2)) {

            } else {
                Toast.makeText(getActivity(), "No such room available", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {
        private static final int VIEW_TYPE_ME = 1;
        private static final int VIEW_TYPE_OTHER = 2;

        private List<ChatMessage> theList;

        public MoviesAdapter(List<ChatMessage> moviesList) {
            this.theList = moviesList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

            MoviesAdapter.MyViewHolder viewHolder = null;
            switch (viewType) {
                case VIEW_TYPE_ME:
                    View viewChatMine = layoutInflater.inflate(R.layout.item_chat_mine, parent, false);
                    viewHolder = new MyViewHolder(viewChatMine);
                    break;
                case VIEW_TYPE_OTHER:
                    View viewChatOther = layoutInflater.inflate(R.layout.item_chat_other, parent, false);
                    viewHolder = new MyViewHolder(viewChatOther);
                    break;
            }
            return viewHolder;

        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            int itemViewType = holder.getItemViewType();

            if (itemViewType == VIEW_TYPE_ME)
            holder.llRowMsg.setBackground(ContextCompat.getDrawable(getContext(), R.mipmap.bubble2));
            else
            holder.llRowMsg.setBackground(ContextCompat.getDrawable(getContext(), R.mipmap.bubble1));

            ChatMessage movie = theList.get(position);
            holder.txtChatMessage.setText(movie.getText());
//            holder.tvMessageStatus.setText(null);
            holder.tvTime.setText(Utility.convertDateToString(new Date(movie.getTimestamp()), "hh:mm"));
        }

        @Override
        public int getItemCount() {
            return theList.size();
        }

        @Override
        public int getItemViewType(int position) {
            String a = theList.get(position).getIdSender();
            String b = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (TextUtils.equals(a, b)) {
                return VIEW_TYPE_ME;
            } else {
                return VIEW_TYPE_OTHER;
            }
        }

        public void add(ChatMessage msg) {
            theList.add(msg);
            notifyItemInserted(theList.size() - 1);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView txtChatMessage, tvTime;//, tvMessageStatus;
            LinearLayout llRowMsg;

            public MyViewHolder(View view) {
                super(view);
                llRowMsg = (LinearLayout) view.findViewById(R.id.llRowMsg);
                txtChatMessage = (TextView) view.findViewById(R.id.tvMsg);
//                tvMessageStatus = (TextView) view.findViewById(R.id.tvMessageStatus);
                tvTime = (TextView) view.findViewById(R.id.tvTime);
            }
        }

    }
}
