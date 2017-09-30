package com.example.eric.chat_firebase.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.eric.chat_firebase.R;
import com.example.eric.chat_firebase.pojo.User;
import com.example.eric.chat_firebase.util.FirebaseConst;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentContacts.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentContacts#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentContacts extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private OnFragmentInteractionListener mListener;

    private ArrayList<User> mList = new ArrayList<User>();
    private MoviesAdapter mAdapter;

    private DatabaseReference users = FirebaseDatabase.getInstance().getReference().child(FirebaseConst.DB_ROOT_USERS);

    public FragmentContacts() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentContacts.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentContacts newInstance(String param1, String param2) {
        FragmentContacts fragment = new FragmentContacts();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

//         adapter = new ArrayAdapter<User>(getActivity(), android.R.layout.simple_dropdown_item_1line,list);
//        User dummy = new User("Beryl", "ada", true);
//        mList.add(dummy);
        mAdapter = new MoviesAdapter(mList);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_contacts, container, false);

        ButterKnife.bind(this, view);

        // show progress
        if (mListener != null) {
            mListener.onLoadContacts();
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        //add listener here
        users.addChildEventListener(listenerChildEvent);
    }

    @Override
    public void onPause() {
        super.onPause();

        //remove listener
        users.removeEventListener(listenerChildEvent);
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
        void onFragmentInteraction(Uri uri);
        void onContactSelected(final User user);

        void onLoadContacts();
    }

    private ChildEventListener listenerChildEvent = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            User user = dataSnapshot.getValue(User.class);

            if (!TextUtils.isEmpty(user.getName())) {

                // tentunya tampilin orang selain yg login
                String key = dataSnapshot.getKey();
                if (TextUtils.equals(key, FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    return;

                    // kalo udah ada cukup update status
                boolean found = false;
                for (int i = 0; i < mList.size(); i++) {
                    if (mList.get(i).getUid().equals(key)) {
                        found = true;

                        mList.set(i, user);
                        break;
                    }

                }

                if (!found) {
                    mList.add(user);
                }

                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            mList.remove(user.getName());
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {

        private List<User> theList;

        public MoviesAdapter(List<User> moviesList) {
            this.theList = moviesList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_chat_contact_list, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            User movie = theList.get(position);
            holder.tvContactName.setText(movie.getName());
            holder.tvUid.setText(movie.getUid());
            holder.tvStatus.setText(movie.getStatus());
            holder.tvIsOnline.setText(movie.isOnline() ? "online" : "offline");
        }

        @Override
        public int getItemCount() {
            return theList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvContactName, tvUid, tvStatus, tvIsOnline;

            public MyViewHolder(View view) {
                super(view);
                tvContactName = (TextView) view.findViewById(R.id.tvContactName);
                tvUid = (TextView) view.findViewById(R.id.tvUid);
                tvStatus = (TextView) view.findViewById(R.id.tvStatus);
                tvIsOnline = (TextView) view.findViewById(R.id.tvIsOnline);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        // Check if an item was deleted, but the user clicked it before the UI removed it
                        if (position == RecyclerView.NO_POSITION) return;

                        if (mListener != null) {
                            User user = theList.get(position);
                            mListener.onContactSelected(user);
                        }

                    }
                });
            }
        }

    }
}
