package com.redeban.payment.example.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redeban.payment.example.R;
import com.redeban.payment.example.rest.model.BackendService;
import com.redeban.payment.example.rest.model.DeleteCardResponse;
import com.redeban.payment.example.rest.model.GetCardsResponse;
import com.redeban.payment.example.rest.model.RetrofitFactory;
import com.redeban.payment.example.utils.Alert;
import com.redeban.payment.example.utils.Constants;
import com.redeban.payment.example.utils.MyCardAdapter;
import com.redeban.payment.model.Card;
import com.redeban.payment.rest.model.ErrorResponse;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListCardsActivity extends AppCompatActivity {

    ArrayList<Card> listCard;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Button buttonAddCard;

    Context mContext;
    BackendService backendService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cards);
        mContext = this;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        buttonAddCard = (Button) findViewById(R.id.buttonAddCard);
        buttonAddCard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddCardActivity.class);
                startActivity(intent);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        backendService = RetrofitFactory.getClient().create(BackendService.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        getCards();
    }

    public void getCards(){

        final ProgressDialog pd = new ProgressDialog(ListCardsActivity.this);
        pd.setMessage("");
        pd.show();

        backendService.getCards(Constants.USER_ID).enqueue(new Callback<GetCardsResponse>() {
            @Override
            public void onResponse(Call<GetCardsResponse> call, Response<GetCardsResponse> response) {
                pd.dismiss();
                GetCardsResponse getCardsResponse = response.body();
                if(response.isSuccessful()) {
                    listCard = (ArrayList<Card>) getCardsResponse.getCards();
                    mAdapter = new MyCardAdapter(listCard, new MyCardAdapter.OnCardSelectedListener() {
                        @Override public void onItemClick(Card card) {

                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("CARD_TOKEN",card.getToken());
                            returnIntent.putExtra("CARD_TYPE",card.getType());
                            returnIntent.putExtra("CARD_LAST4",card.getLast4());
                            setResult(Activity.RESULT_OK,returnIntent);
                            finish();

                        }
                    }, new MyCardAdapter.OnCardDeletedClickListener() {
                        @Override public void onItemClick(Card card) {
                            deleteCard(card);

                        }
                    });
                    mRecyclerView.setAdapter(mAdapter);
                }else {
                    Gson gson = new GsonBuilder().create();
                    try {
                        ErrorResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                        Alert.show(mContext,
                                "Error",
                                errorResponse.getError().getType());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<GetCardsResponse> call, Throwable e) {
                pd.dismiss();
                Alert.show(mContext,
                        "Error",
                        e.getLocalizedMessage());
            }
        });
    }


    public void deleteCard(Card card){

        final ProgressDialog pd = new ProgressDialog(ListCardsActivity.this);
        pd.setMessage("");
        pd.show();

        backendService.deleteCard(Constants.USER_ID, card.getToken()).enqueue(new Callback<DeleteCardResponse>() {
            @Override
            public void onResponse(Call<DeleteCardResponse> call, Response<DeleteCardResponse> response) {
                pd.dismiss();
                DeleteCardResponse deleteCardResponse = response.body();
                if(response.isSuccessful()) {
                    getCards();
                    Alert.show(mContext,
                            "Successfully Deleted Card",
                            deleteCardResponse.getMessage());
                }else {
                    Gson gson = new GsonBuilder().create();
                    try {
                        ErrorResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                        Alert.show(mContext,
                                "Error",
                                errorResponse.getError().getType());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<DeleteCardResponse> call, Throwable e) {
                pd.dismiss();
                Alert.show(mContext,
                        "Error",
                        e.getLocalizedMessage());
            }
        });
    }

}
