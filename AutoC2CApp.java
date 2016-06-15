// import ANTLR runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.util.*;

class C2CVisitor extends AutoCBaseVisitor<Integer> 
{
	Stack<StringBuilder> out = new Stack<StringBuilder>();
	int parenthesiscounter = 0;
	
	C2CVisitor(){
		out.push(new StringBuilder(""));
	}
	@Override public Integer visitDirectiveDefinition(@NotNull AutoCParser.DirectiveDefinitionContext ctx) { 
		if(ctx.getParent() instanceof AutoCParser.IncListContext)
			for (int i=0; i< ctx.getChildCount(); i++)
				out.peek().append(ctx.getChild(i).getText()+"\n");	// Print IncList
		return visitChildren(ctx); 
	}
	@Override public Integer visitFunction(@NotNull AutoCParser.FunctionContext ctx) {
		
		if(ctx.getParent().getParent() instanceof AutoCParser.ProgramContext){
			for (int i=0; i < ctx.getChildCount(); i++)
				if (ctx.getChild(i) instanceof TerminalNode){
					out.peek().append(ctx.getChild(i).getText());		// Print Function without CompoundStmt
					if(ctx.getChild(i).getText().equals("void") || ctx.getChild(i).getText().equals("int") || ctx.getChild(i).getText().equals("auto")){
						out.peek().append(" ");
					}// " " after void
				}
		}
		
		return visitChildren(ctx); 
	}
	@Override public Integer visitTerminal(TerminalNode node) {
		if(node.getText().equals("}")){
			String s = out.pop().toString();
			s = s.substring(0, s.length()-1);
			out.push(new StringBuilder(s));
		}
		
		if(node.getParent().getParent().getParent() instanceof AutoCParser.ProgramContext==false)
			out.peek().append(node.getText()); 			// Print TerminalNode 
		
		if(node.getText().equals("char") || node.getText().equals("void") || node.getText().equals("float") || node.getText().equals("int") || node.getText().equals("return") || node.getText().equals("auto") || node.getText().equals("case") ){
			out.peek().append(" ");
		}// " " after type keyword
		
		
		if(node.getText().equals(";")){
			if(node.getParent() instanceof AutoCParser.ForStmtContext){
				out.peek().append(" ");
			}else{
				out.peek().append("\n");
				for(int i = 0; i< parenthesiscounter; i++){
					out.peek().append("\t");
				}
			}
		} else if(node.getText().equals("{")){
			out.peek().append("\n");
			parenthesiscounter++;
			for(int i = 0; i< parenthesiscounter; i++){
				out.peek().append("\t");
			}
		} else if(node.getText().equals("}")){
			out.peek().append("\n");
			parenthesiscounter--;
			for(int i = 0; i< parenthesiscounter; i++){
				out.peek().append("\t");
			}
			
			if(node.getParent().getParent().getParent() instanceof AutoCParser.WhileStmtContext){
				for(int i=0; i<parenthesiscounter+1; i++){
					String s = out.pop().toString();
					s = s.substring(0, s.length()-1);
					out.push(new StringBuilder(s));
				}	
			}
		} else if(node.getText().equals(":")){
			out.peek().append("\n");
			parenthesiscounter++;
			for(int i = 0; i< parenthesiscounter; i++){
				out.peek().append("\t");
			}
		} else if(node.getText().equals("break;")){
			out.peek().append("\n");
			parenthesiscounter--;
			for(int i = 0; i< parenthesiscounter; i++){
				out.peek().append("\t");
			}
		}
		
		return null;
	}
	
}

public class AutoC2CApp {
	public static void main(String[] args) throws IOException {
		// Input file		
		if (args.length != 1){
			System.err.println("Input C file");
			System.exit(1);
		}
		String filename = args[0];
		System.setIn(new FileInputStream(filename)); 
		InputStream input = System.in;

		// Get lexer
		AutoCLexer lexer = new AutoCLexer(new ANTLRInputStream(input));
		// Get a list of matched tokens
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		// Pass tokens to parser
		AutoCParser parser = new AutoCParser(tokens);
		// ParseTree
		ParseTree tree = parser.program();
		// our visitor
		C2CVisitor visitor = new C2CVisitor();
		// visit AutoCParser
		visitor.visit(tree);
	
		// Output file
		FileOutputStream output = new FileOutputStream(new File("output_"+args[0]));
		System.out.println("Output file name:  output_"+args[0]);
		output.write(visitor.out.peek().toString().getBytes());
		output.flush();
		output.close();
	}
} 
