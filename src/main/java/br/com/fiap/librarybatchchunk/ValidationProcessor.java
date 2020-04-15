package br.com.fiap.librarybatchchunk;

import org.springframework.batch.item.ItemProcessor;

public class ValidationProcessor implements ItemProcessor<Aluno,Aluno>{
	
	private static Integer index = 0;
	
	public Aluno process(Aluno aluno)
    {
		if (aluno.getNome() == "") {
    		return null;
    	}
		
		if (aluno.getNome().substring(0, 2).equals("--")) {
			return null;
    	}
        
		aluno.setNome(aluno.getNome().toUpperCase());
		
		aluno.setId(++index);
		
        return aluno;
    }
}
