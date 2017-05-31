package letv.jmeter.demo;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.InvalidVariableException;

import letv.jmeter.pay.functions.UspayGettoken;

public class LetvJmeterDemo {

	public static void main(String[] args) {
		UspayGettoken ug = new UspayGettoken();
		Collection<CompoundVariable> cc = new LinkedList<CompoundVariable>();
		cc.add(new CompoundVariable("b75letvpaybac731b7elemall68c6125"));
		cc.add(new CompoundVariable("lepay.pc.api.show.cashier.us"));

		try {
			ug.setParameters(cc);
			System.out.println(ug.execute());
		} catch (InvalidVariableException e) {
			e.printStackTrace();
		}
	}

}
