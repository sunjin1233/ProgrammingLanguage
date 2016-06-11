// import ANTLR runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.util.*;

class C2CListener extends CBaseListener 
{
	Stack<StringBuilder> out = new Stack<StringBuilder>();
	CParser parser;
	int counter=0;
	C2CListener(CParser parser){
		out.push(new StringBuilder(""));
		this.parser = parser;
	}
	
	@Override	public void enterDirectiveDefinition(@NotNull CParser.DirectiveDefinitionContext ctx) {
		if(ctx.getParent() instanceof CParser.IncListContext)
			for (int i=0; i< ctx.getChildCount(); i++)
				out.peek().append(ctx.getChild(i).getText()+"\n");	// Print IncList
	}
	
	@Override	public void enterFunction(@NotNull CParser.FunctionContext ctx) { 
		for (int i=0; i < ctx.getChildCount(); i++)
			if (ctx.getChild(i) instanceof TerminalNode)
				out.peek().append(ctx.getChild(i).getText()+" ");		// Print Function without CompoundStmt
	}
	
	@Override	public void visitTerminal(TerminalNode node) {
		if(node.getText().equals("}") && node.getParent() instanceof CParser.CompoundStmtContext){
			out.peek().append("\n");
			if(counter != 0){
				for(int i=0; i<counter; i++){
					out.peek().append("\t");
				}
			}
		}
		
		if(node.getParent().getParent().getParent() instanceof CParser.ProgramContext==false)
			out.peek().append(node.getText()); 			// Print TerminalNode
		
		if(node.getText().equals("char") || node.getText().equals("void") || node.getText().equals("float") || node.getText().equals("int") || node.getText().equals("return")){
			out.peek().append(" ");
		}		
	}
	
	@Override public void enterDeclList(CParser.DeclListContext ctx) {
		if(ctx.getParent() instanceof CParser.CompoundStmtContext){
			out.peek().append("\n");
			out.peek().append("\t");
		}
	}
	
	@Override public void enterDeclaration(CParser.DeclarationContext ctx) {
		if(counter != 0){
			for(int i=0; i<counter; i++){
				out.peek().append("\t");
			}
		}
	}
	
	@Override public void exitDeclaration(CParser.DeclarationContext ctx) { 
		out.peek().append("\n");
		out.peek().append("\t");
	}
	
	@Override public void enterCompoundStmt(CParser.CompoundStmtContext ctx) {
		if(ctx.getParent().getParent() instanceof CParser.WhileStmtContext){
			
		}else if(ctx.getParent() instanceof CParser.FunctionContext){
			out.peek().append("\n");
			if(counter != 0){
				for(int i=0; i<counter; i++){
					out.peek().append("\t");
				}
			}
		}else{
			out.peek().append("\n");
			if(counter != 0){
				for(int i=0; i<counter; i++){
					out.peek().append("\t");
				}
			}
		}
	}
	
	@Override public void enterStmt(CParser.StmtContext ctx) {
		if( ctx.getParent() instanceof CParser.ForStmtContext || ctx.getParent() instanceof CParser.IfStmtContext || ctx.getParent() instanceof CParser.WhileStmtContext){
			
		}else{
			out.peek().append("\n");
			if(counter != 0){
				for(int i=0; i<counter; i++){
					out.peek().append("\t");
				}
			}
			out.peek().append("\t");
		}
	}
	
	@Override public void exitStmt(CParser.StmtContext ctx) { 
		if(ctx.getChild(0) instanceof CParser.RetStmtContext){
			out.peek().append("\n");
		}
	}
	
	@Override public void enterWhileStmt(CParser.WhileStmtContext ctx) { 
		counter++;
	}
	
	@Override public void exitWhileStmt(CParser.WhileStmtContext ctx) { 
		counter--;
	}
	
	@Override public void enterForStmt(CParser.ForStmtContext ctx) { 
		out.peek().append("\n");
		out.peek().append("\t");
		if(counter != 0){
			for(int i=0; i<counter; i++){
				out.peek().append("\t");
			}
		}
		counter++;
	}

	@Override public void exitForStmt(CParser.ForStmtContext ctx) { 
		counter--;
	}

	@Override public void enterIfStmt(CParser.IfStmtContext ctx) { 
		counter++;
	}

	@Override public void exitIfStmt(CParser.IfStmtContext ctx) {
		counter--;
	}
}

public class C2CApp {
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
		CLexer lexer = new CLexer(new ANTLRInputStream(input));
		// Get a list of matched tokens
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		// Pass tokens to parser
		CParser parser = new CParser(tokens);
		// Walk parse-tree and attach our listener
		ParseTreeWalker walker = new ParseTreeWalker();
		C2CListener listener = new C2CListener(parser);
		// walk from the root of parse tree
		walker.walk(listener, parser.program());
	
		// Output file
		FileOutputStream output = new FileOutputStream(new File("output_"+args[0]));
		System.out.println("Output file name:  output_"+args[0]);
		output.write(listener.out.peek().toString().getBytes());
		output.flush();
		output.close();
	}
} 
