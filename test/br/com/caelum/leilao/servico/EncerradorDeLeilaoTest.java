package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;

public class EncerradorDeLeilaoTest {
	
	public Calendar dataAntiga;
	
	@Before
	public void setup() {
		dataAntiga = Calendar.getInstance();
		dataAntiga.set(1991,01,20);
	}
	@Test
	public void deve_encerrar_leiloes_comecados_semana_passada() {
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(dataAntiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataAntiga).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		LeilaoDao mockDao = Mockito.mock(LeilaoDao.class);
		
		when(mockDao.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(mockDao);
		
		encerrador.encerra();
		
		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}
	
	@Test
	public void deve_atualizar_leiloes_encerrados_apenas_uma_vez() {
		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(dataAntiga).constroi();
		
		LeilaoDao mockDao = mock(LeilaoDao.class);
		
		when(mockDao.correntes()).thenReturn(Arrays.asList(leilao1));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(mockDao);
		
		encerrador.encerra();
		
		verify(mockDao, times(1)).atualiza(leilao1);
	}
	
	 @Test
	    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {

	        Calendar ontem = Calendar.getInstance();
	        ontem.add(Calendar.DAY_OF_MONTH, -1);

	        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
	            .naData(ontem).constroi();
	        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
	            .naData(ontem).constroi();

	        LeilaoDao daoFalso = mock(LeilaoDao.class);
	        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

	        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
	        encerrador.encerra();

	        assertEquals(0, encerrador.getTotalEncerrados());
	        assertFalse(leilao1.isEncerrado());
	        assertFalse(leilao2.isEncerrado());

	        verify(daoFalso, atMost(0)).atualiza(leilao1);
	        verify(daoFalso, never()).atualiza(leilao2);
	    }
}
