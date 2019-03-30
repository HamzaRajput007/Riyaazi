package a58bhabscsgcs14.gabrial.gcu.edu.riyaazi;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class NewCalculatorFragment extends Fragment {

    List<String> output = new ArrayList<>();
    ArrayList<String> contents;
    String item;
    View view;
    TextView txtInpuut;
    Button one,two,three,four,five,six,seven,eight,nine,zero,dot,mul,div,add,sub,equal,clear,backSpace;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_first, container, false);

        txtInpuut = view.findViewById(R.id.txtInput);
        one = view.findViewById(R.id.btnOne);
        two = view.findViewById(R.id.btnTwo);
        three = view.findViewById(R.id.btnThree);
        four = view.findViewById(R.id.btnFour);
        five = view.findViewById(R.id.btnFive);
        six = view.findViewById(R.id.btnSix);
        seven = view.findViewById(R.id.btnSeven);
        eight = view.findViewById(R.id.btnEight);
        nine = view.findViewById(R.id.btnNine);
        zero = view.findViewById(R.id.btnZero);
        add = view.findViewById(R.id.btnAdd);
        sub = view.findViewById(R.id.btnSubtract);
        mul = view.findViewById(R.id.btnMultiply);
        div = view.findViewById(R.id.btnDivide);
        dot = view.findViewById(R.id.btnDecimal);
        equal = view.findViewById(R.id.btnEquals);
        clear = view.findViewById(R.id.btnClear);
        backSpace = view.findViewById(R.id.btnBack);
        one.setOnClickListener(v -> txtInpuut.append("1"));
        two.setOnClickListener(v -> txtInpuut.append("2"));
        three.setOnClickListener(v -> txtInpuut.append("3"));
        four.setOnClickListener(v -> txtInpuut.append("4"));
        five.setOnClickListener(v -> txtInpuut.append("5"));
        six.setOnClickListener(v -> txtInpuut.append("6"));
        seven.setOnClickListener(v -> txtInpuut.append("7"));
        eight.setOnClickListener(v -> txtInpuut.append("8"));
        nine.setOnClickListener(v -> txtInpuut.append("9"));
        zero.setOnClickListener(v -> txtInpuut.append("0"));
        add.setOnClickListener(v -> txtInpuut.append("+"));
        mul.setOnClickListener(v -> txtInpuut.append("*"));
        div.setOnClickListener(v -> txtInpuut.append("/"));
        clear.setOnClickListener(v -> txtInpuut.setText(""));
        sub.setOnClickListener(v -> txtInpuut.append("-"));
        dot.setOnClickListener(v -> txtInpuut.append("."));
        backSpace.setOnClickListener(v -> {
            String str = txtInpuut.getText().toString();
            if (str.length() > 1) {
                str = str.substring(0, str.length() - 1);
                txtInpuut.setText(str);
            } else if (str.length() <= 1) {
                txtInpuut.setText("");
            }
        });

           equal.setOnClickListener(v -> btnEqualsPressed());





        return view;
    }

    private void btnEqualsPressed() {
        try{
            String a = txtInpuut.getText().toString();
            if(a==""){
                txtInpuut.setText("No Expression enterd..");
                return;
            }
            a = brackets(a);
            txtInpuut.setText("");
            txtInpuut.setText(a);
            MainActivity.Instance.LoadCardResult(contents.get(0) , output);
        }catch (Exception ex){
            arHelper.ShowToast(ex.getMessage());
        }
    }
    public String brackets(String s){             //method which deal with brackets separately
        while(s.contains(Character.toString('('))||s.contains(Character.toString(')'))){
            for(int o=0; o<s.length();o++){
                try{                                                        //if there is not sign
                    if((s.charAt(o)==')' || Character.isDigit(s.charAt(o))) //between separate brackets
                            && s.charAt(o+1)=='('){                         //or number and bracket,
                        s=s.substring(0,o+1)+"*"+(s.substring(o+1));        //it treat it as
                    }                                                       //a multiplication
                }catch (Exception ignored){}                                //ignore out of range ex
                if(s.charAt(o)==')'){                                  //search for a closing bracket
                    for(int i=o; i>=0;i--){
                        if(s.charAt(i)=='('){                          //search for a opening bracket
                            String in = s.substring(i+1,o);
                            in = recognize(in);
                            s=s.substring(0,i)+in+s.substring(o+1);
                            i=o=0;
                        }
                    }
                }
            }
            if(s.contains(Character.toString('('))||s.contains(Character.toString(')'))||
                    s.contains(Character.toString('('))||s.contains(Character.toString(')'))){
                return "Error: incorrect brackets placement";
            }
        }
        s=recognize(s);
        return s;
    }
    public String recognize(String s){              //method divide String on numbers and operators
        PutIt putIt = new PutIt();
        contents = new ArrayList<>();         //holds numbers and operators
        item = "";
        for(int i=s.length()-1;i>=0;i--){           //is scan String from right to left,
            if(Character.isDigit(s.charAt(i))){     //Strings are added to list, if scan finds
                item=s.charAt(i)+item;              //a operator, or beginning of String
                if(i==0){
                    putIt.put();
                }
            }else{
                if(s.charAt(i)=='.'){
                    item=s.charAt(i)+item;
                }else if(s.charAt(i)=='-' && (i==0 || (!Character.isDigit(s.charAt(i-1))))){
                    item=s.charAt(i)+item;          //this part should recognize
                    putIt.put();                    //negative numbers
                }else{
                    putIt.put();                //it add already formed number and
                    item+=s.charAt(i);          //operators to list
                    putIt.put();                //as separate Strings
                    if(s.charAt(i)=='|'){       //add empty String to list, before "|" sign,
                        item+=" ";          //to avoid removing of any meaningful String
                        putIt.put();        //in last part of result method
                    }
                }
            }
        }
        contents = putIt.result(contents, "^", "|");    //check Strings
        contents = putIt.result(contents, "/", "*");    //for chosen
        contents = putIt.result(contents, "+", "-");    //operators
        return contents.get(0);
    }
    public class PutIt{
        public void put(){
            if(!item.equals("")){
                contents.add(0,item);
                item="";
            }
        }


        public ArrayList<String>result(ArrayList<String> arrayList, String op1, String op2){
            int scale = 10;                              //controls BigDecimal decimal point accuracy
            BigDecimal result = new BigDecimal(0);
            for(int c = 0; c<arrayList.size();c++){
                if(arrayList.get(c).equals(op1)|| arrayList.get(c).equals(op2)){
                    if(arrayList.get(c).equals("^")){
                        result = new BigDecimal(arrayList.get(c-1)).pow(Integer.parseInt(arrayList.get(c+1)));
                        output.add(arrayList.toString().replace(',' , ' '));
                    }else if(arrayList.get(c).equals("|")){
                        result = new BigDecimal(Math.sqrt(Double.parseDouble(arrayList.get(c+1))));
                        output.add(arrayList.toString().replace(',' , ' '));
                    }else if(arrayList.get(c).equals("/")){
                        result = new BigDecimal(arrayList.get(c-1)).divide
                                (new BigDecimal(arrayList.get(c+1)),scale,BigDecimal.ROUND_DOWN);
                        output.add(arrayList.toString().replace(',' , ' '));
                    }else if(arrayList.get(c).equals("*")){
                        result = new BigDecimal(arrayList.get(c-1)).multiply
                                (new BigDecimal(arrayList.get(c+1)));
                        output.add(arrayList.toString().replace(',' , ' '));
                    }else if(arrayList.get(c).equals("+")){
                        result = new BigDecimal(arrayList.get(c-1)).add(new BigDecimal(arrayList.get(c+1)));
                        output.add(arrayList.toString().replace(',' , ' '));
                    }else if(arrayList.get(c).equals("-")){
                        result = new BigDecimal(arrayList.get(c-1)).subtract(new BigDecimal(arrayList.get(c+1)));
                        output.add(arrayList.toString().replace(',' , ' '));
                    }
                    try{       //in a case of to "out of range" ex
                        arrayList.set(c, (result.setScale(scale, RoundingMode.HALF_DOWN).
                                stripTrailingZeros().toPlainString()));
                        arrayList.remove(c + 1);            //it replace the operator with result
                        arrayList.remove(c - 1);              //and remove used numbers from list
                    }catch (Exception ignored){}
                }else{
                    continue;
                }
                c=0;                     //loop reset, as arrayList changed size
            }
            return arrayList;
        }
    }
}

