package br.com.fiap.librarybatchchunk;

import org.springframework.batch.item.ItemProcessor;

public class ValidationProcessor implements ItemProcessor<Pessoa,Pessoa>{

	public Pessoa process(Pessoa pessoa)
    {
		if (pessoa.getNome() == "") {
    		return null;
    	}
		
		if (pessoa.getNome().substring(0, 2).equals("--")) {
			return null;
    	}
        
		pessoa.setNome(pessoa.getNome().toUpperCase());
        pessoa.setCpf(
                pessoa.getCpf()
                        .replaceAll("\\.", "")
                        .replace("-", "")
        );
		
        return pessoa;
    }
}
