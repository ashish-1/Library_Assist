package com.libassist.libraryassist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Database {
    DatabaseHp dbhelp;
    Context con;
    String pattern="dd-MM-yyyy";


    int index;
    NotificationManager nm;

    public Database(Context context){
        dbhelp=new DatabaseHp(context);
        con=context;
    }

    public String today(){
        String date=new SimpleDateFormat(pattern).format(new Date());
        return date;
    }
    public String after(String now){
        SimpleDateFormat sdf=new SimpleDateFormat(pattern);
        int returndate=getreturn();
        Calendar c=Calendar.getInstance();
        try {
            c.setTime(sdf.parse(now));
            c.add(Calendar.DATE,returndate );
            sdf=new SimpleDateFormat(pattern);
            Date newdate=new Date(c.getTimeInMillis());
            return sdf.format(newdate);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }
    public long add(String name) {
        long id;
        String doi,dor;
        SQLiteDatabase sqLiteDatabase=dbhelp.getWritableDatabase();
        ContentValues cn= new ContentValues();
        doi=today();
        dor=after(doi);
        cn.put(dbhelp.BNAME,name);
        cn.put(dbhelp.DOI,doi);
        cn.put(dbhelp.DOR,dor);
        id=sqLiteDatabase.insert(dbhelp.TBNAME,null,cn);
        return id;
    }
    public long check(String dor){
        //run with services.
        SimpleDateFormat sdf=new SimpleDateFormat(pattern);
        Date start=null,end=null;
        String present= today();

        long days=0;
        try{
            start=sdf.parse(present);
            end=sdf.parse(dor);
            long diff = end.getTime() - start.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            days = hours / 24;
        }
        catch(Exception e){
            Toast.makeText(con, "Error", Toast.LENGTH_SHORT).show();
        }
        return days;
    }
    public ArrayList<String> display(){
        String[] colname={dbhelp.BNAME,dbhelp.DOI,dbhelp.DOR};
        ArrayList<String> ar=new ArrayList<>();
        String name,dor;
        SQLiteDatabase sqLiteDatabase=dbhelp.getWritableDatabase();

        Cursor cursor=sqLiteDatabase.query(dbhelp.TBNAME,colname,null,null,null,null,null);
        while(cursor.moveToNext()){
            name=cursor.getString(cursor.getColumnIndex(dbhelp.BNAME));
            dor=cursor.getString(cursor.getColumnIndex(dbhelp.DOR));
            ar.add(name+"\n____________\nReturn Date:"+dor);//need to pass only the book name
             }

        return ar;

    }

    public void diff(){
        //to be run with services
        String[] col={dbhelp.BNAME,dbhelp.DOI,dbhelp.DOR};
        ArrayList<String> ar=new ArrayList<>();
        int count=0;
        index=0;
        SQLiteDatabase sql=dbhelp.getWritableDatabase();
        Cursor cursor= sql.query(dbhelp.TBNAME,col,null,null,null,null,null);

        while(cursor.moveToNext())
        {
            long diff = check(cursor.getString(cursor.getColumnIndex(dbhelp.DOR)));
            if (diff <= 1)
            {
                count++;
                ar.add(cursor.getString(cursor.getColumnIndex(dbhelp.BNAME)));
            }
        }

        if(count>0)
                  notification();

    }

    public void notification(){
        nm=(NotificationManager)con.getSystemService(con.NOTIFICATION_SERVICE);
        Intent in= new Intent(con,booklist.class);

        PendingIntent pi= PendingIntent.getActivity(con,0,in,0);
        NotificationCompat.Builder notify=new NotificationCompat.Builder(con)
                .setSmallIcon(R.drawable.lib)
                .setContentTitle("Hello!!")
                .setContentInfo("You have Books to return ")
                .setContentIntent(pi)
                .setDefaults(Notification.DEFAULT_ALL);
        nm.notify(111,notify.build());
    }


    public void deletebook(String book){
        SQLiteDatabase sqLiteDatabase=dbhelp.getWritableDatabase();
        String[] args={book};
        sqLiteDatabase.delete(dbhelp.TBNAME,dbhelp.BNAME+"=?",args);
    }


    ////FOR DISPLAYING BOOK NAME,DOI,DOR IN THE INTENT WHICH IS USED BY THE LISTACTIVTY BUtFir REISSUE AND RETURN
    Cursor senddata;
    String bn,di,dr;
    public void cursordis(int pos){
        //linked to the listonclicklistener
        String[] col={dbhelp.BNAME,dbhelp.DOI,dbhelp.DOR};
        SQLiteDatabase sql= dbhelp.getWritableDatabase();
        senddata=sql.query(dbhelp.TBNAME,col,null,null,null,null,null);
        senddata.moveToPosition(pos);
        bn=senddata.getString(senddata.getColumnIndex(dbhelp.BNAME));
        di=senddata.getString(senddata.getColumnIndex(dbhelp.DOI));
        dr=senddata.getString(senddata.getColumnIndex(dbhelp.DOR));
    }


    public void Rcheck(String buk){

        SQLiteDatabase sqLiteDatabase=dbhelp.getWritableDatabase();
        String[] colname={dbhelp.BNAME,dbhelp.DOI,dbhelp.DOR};
        String comp;
        Cursor cursor=sqLiteDatabase.query(dbhelp.TBNAME,colname,null,null,null,null,null);
        while(cursor.moveToNext())
        {
            comp=cursor.getString(cursor.getColumnIndex(dbhelp.BNAME));
             if(buk.compareTo(comp)==0)
             {
                deletebook(buk);
                break;
             }
        }

    }

    //LINKED TO THE INTENT TO DISPLAY THE CLICKED ONE BUtFir REISSUE AND RETURN ALSOOO
    public  String sendbook(){  return bn;    }
    public String senddoi(){   return di;    }
    public  String senddor(){   return dr;    }


    //RETURN DATE
  /*  public void changert(int newdate){
        returndate=newdate;
        Toast.makeText(con,"Date Changed",Toast.LENGTH_SHORT).show();
    }*/
    public void putreturn(int date){
        String strdate=date+"";
        try{
            FileOutputStream fileout=con.openFileOutput("date.txt", Context.MODE_PRIVATE);
            fileout.write(strdate.getBytes());
            fileout.close();
        }
        catch(FileNotFoundException f){
            Toast.makeText(con,"Please try again",Toast.LENGTH_SHORT).show();
        }
        catch(IOException e){
            Toast.makeText(con,"Please try again",Toast.LENGTH_SHORT).show();
        }
    }
    public int getreturn(){
        int date=10;
        try{
            FileInputStream filein=con.openFileInput("date.txt");
            StringBuffer buffer=new StringBuffer();
            int read=-1;
            while((read=filein.read())!=-1){
                buffer.append((char)read);
            }
            date=Integer.parseInt(buffer.toString());
            filein.close();
        }
        catch(FileNotFoundException f){
            try{
                String newdate=10+"";
                FileOutputStream fileout=con.openFileOutput("date.txt", Context.MODE_PRIVATE);
                fileout.write(newdate.getBytes());
                fileout.close();
            }
            catch(FileNotFoundException filenot){
                Toast.makeText(con,"Please try again",Toast.LENGTH_SHORT).show();
            }
            catch(IOException writtennot){
                Toast.makeText(con,"Please try again",Toast.LENGTH_SHORT).show();
            }
        }
        catch(IOException e){
            Toast.makeText(con,"Please try again",Toast.LENGTH_SHORT).show();
        }
        return date;
    }

    static public class DatabaseHp extends SQLiteOpenHelper {
        private static final String TBNAME="BOOKS";
        private static final String BNAME="bname";
        private static final String DOI="doi";
        private static final String DOR="dor";
        private static final int DBVER=2;
        private static final String CREATE_TB="Create table "+TBNAME+"("+BNAME+" varchar(256),"+DOI+" varchar(256),"+DOR+" varchar(256));";
        private static final String DROP_TB="Drop table if exists "+TBNAME;
        private  Context con;

        public DatabaseHp(Context context) {
            super(context, TBNAME, null,DBVER );
            con=context;
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            try{
                db.execSQL(CREATE_TB);
               // Toast.makeText(con,"Created",Toast.LENGTH_SHORT).show();
            }
            catch(Exception e){
                Toast.makeText(con,"Failed",Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try{
                db.execSQL(DROP_TB);
                //Toast.makeText(con,"Updated",Toast.LENGTH_SHORT).show();
            }
            catch(Exception e){
                Toast.makeText(con,"Failed Update",Toast.LENGTH_SHORT).show();
            }
        }


    }


}
