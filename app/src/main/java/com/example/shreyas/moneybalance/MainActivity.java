package com.example.shreyas.moneybalance;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String txtInput="";
    private String txtAcc="";
    private int amt,f;
    private ListView list;
    private String strName[];
    private boolean longPress=false,back=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add an account", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addAccount(); // Adding account
            }
        });

        listFresh(); // Clears records with zero balance
        openRecord(); // Displays the records
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (back)
            finish();
        else {
            Toast.makeText(MainActivity.this, "Press Back Again to Exit", Toast.LENGTH_SHORT).show();
            startTimer(1000);
            back=true;
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_addAcc) {
            // Add Account selected
            addAccount(); // Adding records
            return true;
        }
        if (id == R.id.action_refresh) {
            // Refresh Selected
            listFresh(); // Refreshing list
            openRecord(); // and displaying it
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startTimer(final long msec) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(msec);
                    back=false;
                }
                catch (Exception e) {
                    Toast.makeText(MainActivity.this,"Timer Error",Toast.LENGTH_SHORT).show();
                }
            }
        });
        thread.start();
    }
    public void openRecord() {
        // Reads and displays the record
        String s=read(); // whole content of the file returned as string
        if (s==null||s.length()==0) {
            // file not found or I/O error has occurred
            String info[]={"No record found !"};
            ListView tmp=(ListView)findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,info);
            tmp.setAdapter(adapter);
            return ;
        }
        txtAcc=s;
        strName=txtAcc.split("\n"); // splitting the string into number of records

        list = (ListView)findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,strName);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long l) {
                if (!longPress) {
                    String name=(String)adapter.getItemAtPosition(pos);
                    if (name.length()>5&&name.indexOf(':')!=-1)
                        addMoney(name,pos);
                }
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View view, int pos, long l) {
                longPress=true;
                String name=(String)adapter.getItemAtPosition(pos);
                if (name.length()>5&&name.indexOf(':')!=-1)
                    deleteAccount(name,pos);
                return false;
            }
        });
    }
    public void deleteAccount(final String data,final int pos) {
        AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
        a_builder.setMessage("Are you sure you want to Delete the Current Account")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String s[]=read().split("\n");
                        s[pos]=data.substring(0,data.lastIndexOf(':'))+String.format(": Rs. %d ",0); // making updates in amount of record

                        write("",false); // resetting the file or creating an empty file
                        // rewriting the changed records to the file
                        for (String str:s)
                            write(str+"\n",true);
                        listFresh();
                        openRecord();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alert = a_builder.create();
        alert.setTitle("Delete Account");
        alert.show();

        longPress=false;
    }
    public void addAccount(){
        // Adding Account if possible
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Account");
        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                txtInput = input.getText().toString();
                int rs=0; // Setting zero as default openning balance
                write(txtInput+String.format("\t: Rs. %d \n",rs),true); // Saving record in file
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    public void addMoney(final String data,final int pos){
        // Adding or Updating amount of a record
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title=data.substring(0,data.lastIndexOf(':'));
        builder.setTitle(title+" : Update Amount");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        builder.setView(input);
        txtInput="0";

        // Set up the buttons
        builder.setPositiveButton("Add (+)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                txtInput = input.getText().toString();
                f=1;
                moneyUpdate(data,pos); // Updating the money
            }
        });
        builder.setNegativeButton("Sub (-)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                txtInput = input.getText().toString();
                f=-1;
                moneyUpdate(data,pos); // Updating the money
            }
        });
        builder.show();
    }
    public void moneyUpdate(String data,int pos) {
        // Main Logic for updating money
        try {
            if (f==0)
                return; // false function call hence do nothing but return
            // function called from addMoney()
            amt=Integer.valueOf(txtInput);
            amt*=f;
            f=0;
        }
        catch (Exception e) {
            // Probably input data was not a number
            Toast.makeText(MainActivity.this,"Wrong Input !",Toast.LENGTH_SHORT).show();
            return ;
        }
        try {
            if (data.length()<5)
                return;
            int ip=data.lastIndexOf(':')+6; // initial position or index
            int fp=data.length(); // final position or index
            int rs=Integer.valueOf(data.substring(ip,fp-1)); // extracting and parsing the string in the range i.e the previous amount
            rs+=amt;
            amt=0;
            String s[]=read().split("\n");
            s[pos]=data.substring(0,data.lastIndexOf(':'))+String.format(": Rs. %d ",rs); // making updates in amount of record

            write("",false); // resetting the file or creating an empty file
            // rewriting the changed records to the file
            for (String i:s)
                write(i+"\n",true);
            openRecord();
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this,"Error in Parsing data",Toast.LENGTH_SHORT).show();
        }
    }
    public void listFresh() {
        // removes unnecessary records
        String s=read();
        if (s==null||s.length()==0) {
            return ;
        }

        String str[]=s.split("\n");
        write("",false); // resetting the file
        for (int i=0;i<str.length;i++) {
            if (str[0].length()>5)
            {
                int ip=str[i].lastIndexOf(':')+6; // initial position or index
                int fp=str[i].length(); // final position or index
                int rs=Integer.valueOf(str[i].substring(ip,fp-1)); // extracting and parsing the string in the range i.e the previous amount
                if (rs!=0)
                    write(str[i]+"\n",true);
            }
        }
    }

    public boolean write(String dat,boolean flag) {
        // writes record name
        try {
            String path="/sdcard/moneybal.dat";
            File file = new File(path);
            if (!file.exists()) {
                // app executed for first time.
                Toast.makeText(MainActivity.this,"New File Created",Toast.LENGTH_SHORT).show();
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(),flag);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(dat);
            bw.close();
            openRecord(); // displays the records
            return true;
        }
        catch (IOException e) {
            Toast.makeText(MainActivity.this,"File I/O error",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    public String read(){
        // Reading the content of the file
        BufferedReader br = null;
        String resp = null;
        try {
            String output="";
            String path="/sdcard/moneybal.dat";

            br = new BufferedReader(new FileReader(path));
            String line="";
            while ((line=br.readLine())!=null){
                output=output+line+"\n";
            }
            resp = output;
        }
        catch (FileNotFoundException e) {
            // app executed for the first time
            Toast.makeText(MainActivity.this,"No File Found",Toast.LENGTH_SHORT).show();
            return null;
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }

        return resp;
    }

}