package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.InOrder;
import org.mockito.Mockito;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;

public class EncerradorDeLeilaoTest {
	
	public Calendar dataAntiga;
	
	public LeilaoDao mockDao;
	
	public EnviadorDeEmail mockCarteiro;
	
	public EncerradorDeLeilao encerrador;
	
	@Before
	public void setup() {
		dataAntiga = Calendar.getInstance();
		dataAntiga.set(1991,01,20);
		mockDao = Mockito.mock(LeilaoDao.class);
		mockCarteiro = mock(EnviadorDeEmail.class);
		encerrador = new EncerradorDeLeilao(mockDao, mockCarteiro);
	}
	
	@Test
	public void deve_encerrar_leiloes_comecados_semana_passada() {
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(dataAntiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataAntiga).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		
		when(mockDao.correntes()).thenReturn(leiloesAntigos);
				
		encerrador.encerra();
		
		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}
	
	@Test
	public void deve_atualizar_leiloes_encerrados_apenas_uma_vez() {
		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(dataAntiga).constroi();
		
		when(mockDao.correntes()).thenReturn(Arrays.asList(leilao1));
				
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

	        encerrador.encerra();

	        assertEquals(0, encerrador.getTotalEncerrados());
	        assertFalse(leilao1.isEncerrado());
	        assertFalse(leilao2.isEncerrado());

	        verify(daoFalso, atMost(0)).atualiza(leilao1);
	        verify(daoFalso, never()).atualiza(leilao2);
	    }
	 
	 @Test
	 public void deve_encerrar_leiloes_e_enviar_email() {
		Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(dataAntiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataAntiga).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(mockDao.correntes()).thenReturn(leiloesAntigos);
		encerrador.encerra();
		
		InOrder inOrder = inOrder(mockDao, mockCarteiro);
		inOrder.verify(mockDao, times(1)).atualiza(leilao1);
		inOrder.verify(mockCarteiro, times(1)).envia(leilao1);
		
		inOrder.verify(mockDao, times(1)).atualiza(leilao2);
		inOrder.verify(mockCarteiro, times(1)).envia(leilao2);
		
		
	 }
	 
	 @Test
	 public void deve_lancar_continuar_execucao_apos_lancar_excecao() {
		 
		 Leilao leilao1 = new CriadorDeLeilao().para("TV").naData(dataAntiga).constroi();
		 Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataAntiga).constroi();
		
		 List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		 when(mockDao.correntes()).thenReturn(leiloesAntigos);
		 		 
		 doThrow(new RuntimeException()).when(mockDao).atualiza(leilao1);
		 encerrador.encerra();
		 verify(mockDao).atualiza(leilao2);
	     verify(mockCarteiro).envia(leilao2);

	 }
}
