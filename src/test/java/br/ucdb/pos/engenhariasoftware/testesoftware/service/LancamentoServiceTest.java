package br.ucdb.pos.engenhariasoftware.testesoftware.service;

import br.ucdb.pos.engenhariasoftware.testesoftware.controller.vo.LancamentoVO;
import br.ucdb.pos.engenhariasoftware.testesoftware.controller.vo.ResultadoVO;
import br.ucdb.pos.engenhariasoftware.testesoftware.converter.StringToDateConverter;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.Lancamento;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento.ENTRADA;
import static br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento.SAIDA;
import static br.ucdb.pos.engenhariasoftware.testesoftware.util.Constantes.DD_MM_YYYY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class LancamentoServiceTest {
    @Mock
    private LancamentoService lancamentoService;

    @BeforeClass(alwaysRun = true)
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(lancamentoService.getTotalEntrada(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.getTotalSaida(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.somaValoresPorTipo(anyListOf(Lancamento.class), any(TipoLancamento.class))).thenCallRealMethod();
        when(lancamentoService.getResultadoVO(anyListOf(Lancamento.class), anyInt(), anyLong())).thenCallRealMethod();
        given(lancamentoService.buscaAjax(anyString())).willCallRealMethod();
    }

    @DataProvider(name = "lancamentos")
    protected Object[][] getLancamentos(ITestContext context) {
        List<String> includedGroups = Arrays.asList(context.getIncludedGroups());
        List<Lancamento> lancamentosDefault = Arrays.asList(
                new LancamentoBuilder().comId((long) 1).comDescricao("Entrada 1").comData("05/07/2018").comValor(5000.00).comTipo(ENTRADA).build(),
                new LancamentoBuilder().comId((long) 2).comDescricao("Entrada 2").comData("05/07/2018").comValor(170.00).comTipo(ENTRADA).build(),
                new LancamentoBuilder().comId((long) 3).comDescricao("Entrada 3").comData("05/07/2018").comValor(75.00).comTipo(ENTRADA).build(),
                new LancamentoBuilder().comId((long) 4).comDescricao("Saída 2").comData("05/07/2018").comValor(1227.00).comTipo(SAIDA).build(),
                new LancamentoBuilder().comId((long) 5).comDescricao("Saída 3").comData("06/07/2018").comValor(876.78).comTipo(SAIDA).build(),
                new LancamentoBuilder().comId((long) 6).comDescricao("Saída 4").comData("06/07/2018").comValor(455.78).comTipo(SAIDA).build(),
                new LancamentoBuilder().comId((long) 7).comDescricao("Saída 5").comData("05/07/2018").comValor(455.78).comTipo(SAIDA).build(),
                new LancamentoBuilder().comId((long) 8).comDescricao("Saída 6").comData("10/07/2018").comValor(296.88).comTipo(SAIDA).build(),
                new LancamentoBuilder().comId((long) 9).comDescricao("Saída 7").comData("08/07/2018").comValor(120.00).comTipo(SAIDA).build(),
                new LancamentoBuilder().comId((long) 10).comDescricao("Saída 8").comData("13/07/2018").comValor(94.00).comTipo(SAIDA).build()
        );
        return new Object[][]{
                new Object[]{this.getLancamentosCenario(this.getQtdeLancamentosCenario(includedGroups.get(0)), lancamentosDefault)}
        };
    }

    /**
     * Método que devolve a lista padrão de acordo com a quantidade esperada no cenário
     *
     * @param qtdeLancamentosCenario quantidade de registro a ser retornada
     * @param lancamentosDefault     lista de lançamentos padrão
     * @return lista de lançamentos a ser usada para testar o cenário
     */
    private List<Lancamento> getLancamentosCenario(int qtdeLancamentosCenario, List<Lancamento> lancamentosDefault) {
        List<Lancamento> lancamentosCenario = new ArrayList<>();
        for (int i = 0; i < lancamentosDefault.size(); i++) {
            if (i < qtdeLancamentosCenario)
                lancamentosCenario.add(lancamentosDefault.get(i));
        }
        return lancamentosCenario;
    }

    /**
     * Método que devolve a quantidade de lançamentos esperada baseada no número do grupo de teste
     *
     * @param group nome do grupo de testes
     * @return quantidade de lançamentos do cenário de teste
     */
    private int getQtdeLancamentosCenario(String group) {
        return Integer.parseInt(group.replaceAll("\\D+", ""));
    }

    /*======================================================= Cenário com 10 lançamentos =============================================================================*/
    @Test(dataProvider = "lancamentos", groups = "cenario10")
    public void buscaAjax10LancamentosTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.getTotalEntradaTest(lancamentos, BigDecimal.valueOf(5245.0));
        this.getTotalSaidaTest(lancamentos, BigDecimal.valueOf(3526.22));
        this.getTamanhoListaTest(lancamentos, 10);
        this.getResultadoVOTest(lancamentos);
    }

    /*======================================================= Cenário com 9 lançamentos =============================================================================*/
    @Test(dataProvider = "lancamentos", groups = "cenario9")
    public void buscaAjax9LancamentosTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.getTotalEntradaTest(lancamentos, BigDecimal.valueOf(5245.0));
        this.getTotalSaidaTest(lancamentos, BigDecimal.valueOf(3432.22));
        this.getTamanhoListaTest(lancamentos, 9);
        this.getResultadoVOTest(lancamentos);
    }

    /*======================================================= Cenário com 3 lançamentos =============================================================================*/
    @Test(dataProvider = "lancamentos", groups = "cenario3")
    public void buscaAjax3LancamentosTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.getTotalEntradaTest(lancamentos, BigDecimal.valueOf(5245.0));
        this.getTotalSaidaTest(lancamentos, BigDecimal.valueOf(0));
        this.getTamanhoListaTest(lancamentos, 3);
        this.getResultadoVOTest(lancamentos);
    }

    /*======================================================= Cenário com 1 lançamentos =============================================================================*/
    @Test(dataProvider = "lancamentos", groups = "cenario1")
    public void buscaAjax1LancamentosTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.getTotalEntradaTest(lancamentos, BigDecimal.valueOf(5000.00));
        this.getTotalSaidaTest(lancamentos, BigDecimal.valueOf(0));
        this.getTamanhoListaTest(lancamentos, 1);
        this.getResultadoVOTest(lancamentos);
    }

    /*======================================================= Cenário com 0 lançamentos =============================================================================*/
    @Test(dataProvider = "lancamentos", groups = "cenario0")
    public void busca0LancamentosAjaxTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.getTotalEntradaTest(lancamentos, BigDecimal.valueOf(0));
        this.getTotalSaidaTest(lancamentos, BigDecimal.valueOf(0));
        this.getTamanhoListaTest(lancamentos, 0);
        this.getResultadoVOTest(lancamentos);
    }

    /**
     * Método de teste responsável por validar o total de entradas da lista de lançamentos
     *
     * @param lancamentos lista de lançamento criada para o teste
     */
    private void getTotalEntradaTest(List<Lancamento> lancamentos, BigDecimal totalEsperado) {
        assertEquals(lancamentoService.getTotalEntrada(lancamentos), totalEsperado);
        verify(lancamentoService, atLeastOnce()).getTotalEntrada(anyListOf(Lancamento.class));
    }

    /**
     * Método de teste responsável por validar o total de entradas da lista de lançamentos
     *
     * @param lancamentos lista de lançamento criada para o teste
     */
    private void getTotalSaidaTest(List<Lancamento> lancamentos, BigDecimal totalEsperado) {
        final BigDecimal totalObtido = lancamentoService.getTotalSaida(lancamentos);
        assertEquals(totalObtido, totalEsperado);
        verify(lancamentoService, atLeastOnce()).getTotalSaida(anyListOf(Lancamento.class));
    }

    /**
     * Teste responsável por validar o tamanho da lista de lançamentos
     *
     * @param lancamentos lista de lançamento criada para o teste
     */
    private void getTamanhoListaTest(List<Lancamento> lancamentos, long tamanhoEsperado) {
        when(lancamentoService.busca(anyString())).thenReturn(lancamentos);
        when(lancamentoService.conta(anyString())).thenReturn((long) lancamentos.size());
        long tamanhoObtido = lancamentoService.buscaAjax(anyString()).getTotalRegistros();
        String mensagem = "Era espero o tamanho da lista de lançamentos de " + tamanhoEsperado + " registro(s), mas foi retornado " + tamanhoObtido + " registro(s).";
        assertEquals(tamanhoObtido, tamanhoEsperado, mensagem);
        verify(lancamentoService, atLeastOnce()).buscaAjax(anyString());
    }

    /**
     * Método de teste responsával por garantir que todo atributo da classe Lancamento esteja na LancamentoVO e não nula
     *
     * @param lancamentos lista de lançamento criada para o teste
     */
    private void getResultadoVOTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        when(lancamentoService.busca(anyString())).thenReturn(lancamentos);
        when(lancamentoService.conta(anyString())).thenReturn((long) lancamentos.size());
        ResultadoVO resultadoVO = lancamentoService.buscaAjax(anyString());
        // Obtém a lista de atributos da classe Lancamento
        Field[] campos = Lancamento.class.getDeclaredFields();
        //Percorre a lista de resultado da busca para fazer as validações
        for (LancamentoVO lancamentoVO : resultadoVO.getLancamentos()) {
            for (Field campo : campos) {
                String atributo = campo.getName();
                // Verifica se cada atributo da classe lançamento está na classe LancamentoVO
                assertTrue(doesObjectContainField(lancamentoVO, atributo), "Atributo " + atributo + " não existe na Classe LancamentoVO.");
                //Verificar se cada atributo não está vazio
                String valorAtributo = getMethodValue(lancamentoVO, atributo).toString();
                assertTrue(!valorAtributo.equals("") && valorAtributo != null, "Atributo " + atributo + " é nulo na Classe Lancamento VO.");
            }
        }
        verify(lancamentoService, atLeastOnce()).buscaAjax(anyString());
    }


    /**
     * Método que invoca o método get do atributo informado de um classe
     *
     * @param object   objeto a ser verificado
     * @param atributo atributo a ser buscado seu respectivo método get
     * @return valor do atributo
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object getMethodValue(Object object, String atributo) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            String getMethodtext = "get" + atributo.substring(0, 1).toUpperCase() + atributo.substring(1);
            Method getMethod = object.getClass().getMethod(getMethodtext, new Class[]{});
            return getMethod.invoke(object, new Object[]{});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Método que busca em uma classe por um atributo específico (privado)
     *
     * @param object    objeto a ser verificado
     * @param fieldName nome do campo a ser verificado
     * @return true se encontrado, e false caso contrário
     */
    private boolean doesObjectContainField(Object object, String fieldName) {
        return Arrays.stream(object.getClass().getDeclaredFields()).anyMatch(f -> f.getName().equals(fieldName));
    }

    /**
     * Classe LancamentoBuilder
     * Responsável por construir um objeto da classe Lancamento usado no DataProvider
     */
    static class LancamentoBuilder {
        private Lancamento lancamento;

        LancamentoBuilder() {
            lancamento = new Lancamento();
        }

        LancamentoBuilder comId(Long id) {
            lancamento.setId(id);
            return this;
        }

        LancamentoBuilder comData(String data) {
            StringToDateConverter stringToDate = new StringToDateConverter();
            lancamento.setDataLancamento(stringToDate.convert(data));
            return this;
        }

        LancamentoBuilder comDescricao(String descricao) {
            lancamento.setDescricao(descricao);
            return this;
        }

        LancamentoBuilder comValor(double valor) {
            lancamento.setValor(BigDecimal.valueOf(valor));
            return this;
        }

        LancamentoBuilder comTipo(TipoLancamento tipo) {
            lancamento.setTipoLancamento(tipo);
            return this;
        }

        Lancamento build() {
            return lancamento;
        }
    }
}
