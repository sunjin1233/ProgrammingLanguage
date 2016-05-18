// import ANTLR runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// import Java Map Libs
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
// import Java Stack Libs
import java.util.Stack;

// import Java console IO
import java.io.Console;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;



class EvalListener extends ExprBaseListener {
   //static variables
   static int assignchecker=0;
   static int exprchecker=0;
   static int assignVal;
   static String assignVar;
   
   // hash-map for variables' integer value for assignment
   static Map<String, Integer> vars = new HashMap<String, Integer>(); 
   // stack for expression tree evaluation
   Stack<Integer> evalStack = new Stack<Integer>();
   // stack for operators (+-*/) in shunting-yard algorithm 
   Stack<String> opStack = new Stack<String>();
   // queue for postfix expression
   Queue<String> postQueue = new LinkedList<String>();

   @Override
   public void exitProg(ExprParser.ProgContext ctx) {
      System.out.println("exitProg: "); 
      //pop when operator stack is empty
      while(!opStack.empty()){
    	  postQueue.add(opStack.pop());
      }
      
      //check shunting yard working right
      /*while(!postQueue.isEmpty()){
    	  	System.out.println(postQueue.poll());
      }*/
      
         
      //hashmap 확인
      /*java.util.Iterator<String> var = vars.keySet().iterator();
      while(var.hasNext()){
    	  String variable = var.next();
    	  System.out.println(String.format("%s=%d", variable, vars.get(variable)));
      }*/
      
      //evaluation postfix expression
      if(exprchecker!=0){
    	  EvalPostfix();
      }
      
      //mkae exprchecker 0
      exprchecker=0;
   }
   
   public void EvalPostfix(){
	   int op1, op2,result;
	   
	   while( !postQueue.isEmpty() ){  
		   String s = postQueue.poll();
		   
		   if(s.equals("+")){
			   op2=evalStack.pop();
			   op1=evalStack.pop();
			   result=op1+op2;
			   evalStack.push(result);
		   } else if(s.equals("-")){
			   op2=evalStack.pop();
			   op1=evalStack.pop();
			   result=op1-op2;
			   evalStack.push(result);
		   } else if(s.equals("*")){
			   op2=evalStack.pop();
			   op1=evalStack.pop();
			   result=op1*op2;
			   evalStack.push(result);
		   } else if(s.equals("/")){
			   op2=evalStack.pop();
			   op1=evalStack.pop();
			   result=op1/op2;
			   evalStack.push(result);
		   } else{
			   if( !s.matches("[0-9]+") ){
				   evalStack.push(vars.get(s));
			   } else{
				   evalStack.push(Integer.parseInt(s));
			   }	   
		   }
	   }
	   try{
		   BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt", true));
		   bw.write( String.valueOf( evalStack.pop() ) );
		   bw.newLine();
		   bw.flush();
		   bw.close();
	   }catch(IOException e){
		   
	   }
   }
   
   @Override
   public void exitExpr(ExprParser.ExprContext ctx) {
      System.out.println("exitExpr: "); 
      exprchecker++;
   }

   @Override
   public void enterAssn(ExprParser.AssnContext ctx) {
      System.out.println("enterAssn: ");
      assignchecker++;
   }
   @Override
   public void exitAssn(ExprParser.AssnContext ctx) {
      System.out.println("exitAssn: ");
      assignchecker--;
      vars.put(assignVar, assignVal);
      
      while(!opStack.empty()){
    	  opStack.pop();
      }
      
      while(!postQueue.isEmpty()){
    	  	postQueue.poll();
      }
   }

   // Add more overrride methods if needed 
 
   @Override 
   public void visitTerminal(TerminalNode node) {
      String s = node.getText();

      switch(s) {
      case "+": 
          System.out.println("Terminal PLUS");
          while(!opStack.empty() && (opStack.peek().equals("+") || opStack.peek().equals("-") || opStack.peek().equals("*") || opStack.peek().equals("/")) ){
        	  postQueue.add(opStack.pop());
          }
          opStack.push("+");
          break;
      case "-": 
          System.out.println("Terminal MINUS");
          while(!opStack.empty() && (opStack.peek().equals("+") || opStack.peek().equals("-") || opStack.peek().equals("*") || opStack.peek().equals("/")) ){
        	  postQueue.add(opStack.pop());
          }
          opStack.push("-");
          break;
      case "*": 
          System.out.println("Terminal MULTIPLY");
          while(!opStack.empty() && (opStack.peek().equals("*") || opStack.peek().equals("/")) ){
        	  postQueue.add(opStack.pop());
          }
          opStack.push("*");
          break;
      case "/": 
          System.out.println("Terminal DIVIDE");
          while(!opStack.empty() && (opStack.peek().equals("*") || opStack.peek().equals("/")) ){
        	  postQueue.add(opStack.pop());
          }
          opStack.push("/");
          break;
      case "(": 
          System.out.println("Terminal LEFT_PAR");
          opStack.push("(");
          break;
      case ")": 
          System.out.println("Terminal RIGHT_PAR");
          while(!opStack.peek().equals("(")){
        	  postQueue.add(opStack.pop());
          }
          opStack.pop();
          break;
      default:
          if (s.matches("[0-9]+")) { // INT
             System.out.println("Terminal-INT " + s);
             if(assignchecker != 0){
            	 assignVal=Integer.parseInt(s);
             }
             postQueue.add(s);
          } else if(s.equals(";")||s.equals("=")){	  
          } else { // assignVar
             System.out.println("Terminal-ID " + s);
             if(assignchecker != 0){
            	 assignVar=s;
             }
             postQueue.add(s);
          } 
      }
   }
}

public class ExprEvalApp {
   public static void main(String[] args) throws IOException {
      System.out.println("** Expression Eval w/ antlr-listener **");

      Console c = System.console();
      if (c == null) {
         System.err.println("No Console");
         System.exit(1);
      }
      
      ParseTreeWalker walker = new ParseTreeWalker();
      EvalListener listener = new EvalListener();
      
      BufferedReader br = new BufferedReader(new FileReader("input.txt"));
      while(true){
		  String input = br.readLine();
		  if(input==null) break;
		  //input += '\n';
		
		  // Get lexer
		  ExprLexer lexer = new ExprLexer(new ANTLRInputStream(input));
		  // Get a list of matched tokens
		  CommonTokenStream tokens = new CommonTokenStream(lexer);
		  // Pass tokens to parser
		  ExprParser parser = new ExprParser(tokens);
		  // Walk parse-tree and attach our listener
		 
		  walker.walk(listener, parser.prog());	// walk from the root of parse tree
      }
      br.close();
   }
} 
