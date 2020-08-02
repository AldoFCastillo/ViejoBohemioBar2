package com.example.viejobohemiobar.view.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.viejobohemiobar.R;
import com.example.viejobohemiobar.model.pojo.Order;
import com.example.viejobohemiobar.model.pojo.OrderLog;
import com.example.viejobohemiobar.model.pojo.Product;
import com.example.viejobohemiobar.model.pojo.Result;
import com.example.viejobohemiobar.service.ConfigRecyclerView;
import com.example.viejobohemiobar.view.adapter.ProductAdapter;
import com.example.viejobohemiobar.viewModel.ResultViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderFragment extends Fragment implements ProductAdapter.adapterListener {

    private static final String ARG_SERIAL = "serial";
    public static final String ARG_PATH = "path";


    private Result result;
    private Order order;
    private ProductAdapter productAdapter;
    private String table = "1";
    private ResultViewModel resultViewModel;
    private listener listener;
    private String path;
    private String button;
    private String next;

    @BindView(R.id.recyclerViewOrder)
    RecyclerView recyclerViewOrder;
    @BindView(R.id.buttonConfirmOrder)
    Button buttonConfirmOrder;
    @BindView(R.id.buttonCancelOrder)
    Button buttonCancelOrder;
    @BindView(R.id.editTextCommentsOrder)
    EditText editTextCommentsOrder;
    @BindView(R.id.checkBoxNeedWait)
    CheckBox checkBoxNeedWait;
    @BindView(R.id.textViewTotalOrder)
    TextView textViewTotalOrder;
    @BindView(R.id.buttonToProcessOrder)
    Button buttonToProcessOrder;

    public OrderFragment() {
        // Required empty public constructor
    }

    public static OrderFragment newInstance(Serializable result, String path) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SERIAL, result);
        args.putString(ARG_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().getSerializable(ARG_SERIAL) instanceof Result) {
                this.result = (Result) getArguments().getSerializable(ARG_SERIAL);
                this.order = null;
            } else{
                this.order = (Order) getArguments().getSerializable(ARG_SERIAL);
                this.path = getArguments().getString(ARG_PATH);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        ButterKnife.bind(this, view);

        resultViewModel = ViewModelProviders.of(this).get(ResultViewModel.class);
        recyclerViewOrder = ConfigRecyclerView.getRecyclerView(recyclerViewOrder, getContext());

        if (order != null) {
            setMadeOrder();
        } else setToConfirmOrder();


        return view;
    }

    private void setMadeOrder() {
        productAdapter = new ProductAdapter(OrderFragment.this, order.getResult().getResults());
        recyclerViewOrder.setAdapter(productAdapter);
        switch(path){
            case "p": button = "Tomar Pedido";
            next = "i";
                break;
            case "i": button = "Pedido Entregado";
                next = "c";
                break;
            case "c": button = "Eliminar Pedido";
                break;
        }
        buttonConfirmOrder.setText(button);
        buttonConfirmOrder.setVisibility(View.VISIBLE);
        buttonConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferOrderToProcess();
            }
        });

        textViewTotalOrder.setText(order.getTotal());
        //set RadioButtons
        editTextCommentsOrder.setText(order.getComments());
        checkBoxNeedWait.setActivated(order.getCallWait());
        buttonCancelOrder.setVisibility(View.INVISIBLE);
        buttonConfirmOrder.setVisibility(View.INVISIBLE);
    }

    private void transferOrderToProcess(){
        resultViewModel.getOrderLog(path).observe(getViewLifecycleOwner(), new Observer<OrderLog>() {
            @Override
            public void onChanged(OrderLog orderLog) {
                List<Order> orderList = orderLog.getOrderList();
                orderList.remove(order);
                orderLog.setOrderList(orderList);
                resultViewModel.updateOrderLog(orderLog, path);
                resultViewModel.getOrderLog(next).observe(getViewLifecycleOwner(), new Observer<OrderLog>() {
                    @Override
                    public void onChanged(OrderLog orderLog) {
                        List<Order> orderList = orderLog.getOrderList();
                        orderList.add(order);
                        orderLog.setOrderList(orderList);
                        resultViewModel.updateOrderLog(orderLog, next);
                    }
                });
                //back to StaffFragment
            }
        });
    }

    private void setToConfirmOrder() {
        productAdapter = new ProductAdapter(OrderFragment.this, result.getResults());
        recyclerViewOrder.setAdapter(productAdapter);

        buttonToProcessOrder.setVisibility(View.INVISIBLE);
        buttonCancelOrder.setVisibility(View.VISIBLE);
        buttonConfirmOrder.setVisibility(View.VISIBLE);

        Double total = 0.0;
        for (Product product : result.getResults()) {
            String stringPrice = product.getPrice().substring(1);
            double doble = Double.parseDouble(stringPrice);
            total = total + doble;
        }
        String stringTotal = "$" + total;

        textViewTotalOrder.setText(stringTotal);
        buttonConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comments = editTextCommentsOrder.getText().toString();
                FirebaseAuth fAuth = FirebaseAuth.getInstance();
                String id = fAuth.getUid();
                Order order = Order.getOrderInstance(result, stringTotal, true, checkBoxNeedWait.isChecked(), comments, id, table, getTime());
                confirmOrder(order, "p");

            }
        });

        buttonCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteActualOrder().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
                            Toast.makeText(getContext(), "Pedido Cancelado", Toast.LENGTH_SHORT).show();
                            listener.orderFragmentListener();
                        } else
                            Toast.makeText(getContext(), "Ocurrio un error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }


    private String getTime() {
        Calendar calendario = Calendar.getInstance();
        int hour = calendario.get(Calendar.HOUR_OF_DAY);
        int minutes = calendario.get(Calendar.MINUTE);
        String time = (hour + ":" + minutes);
        return time;
    }

    private void confirmOrder(Order order, String path) {
        resultViewModel.getOrderLog(path).observe(getViewLifecycleOwner(), new Observer<OrderLog>() {
            @Override
            public void onChanged(OrderLog orderLog) {
                List<Order> orderList;
                if (orderLog == null) {
                    orderLog = new OrderLog();
                    orderList = new ArrayList<>();
                } else orderList = orderLog.getOrderList();
                orderList.add(order);
                orderLog.setOrderList(orderList);
                updateOrderLog(orderLog, "p");

                //TODO PEDIR PASSWORD
            }
        });
    }

    private void updateOrderLog(OrderLog orderLog, String path) {
        resultViewModel.updateOrderLog(orderLog, path).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    Toast.makeText(getContext(), "Pedido Confirmado", Toast.LENGTH_SHORT).show();
                    deleteActualOrder();
                    listener.orderFragmentListener();
                } else Toast.makeText(getContext(), "Ocurrio un error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private LiveData<Boolean> deleteActualOrder() {
        MutableLiveData<Boolean> liveBool = new MutableLiveData();
        resultViewModel.deleteActualOrder().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                liveBool.setValue(aBoolean);
            }
        });
        return liveBool;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.listener = (listener) context;
    }

    @Override
    public void selection(Integer adapterPosition, Result result) {

    }

    public interface listener {
        void orderFragmentListener();
    }
}