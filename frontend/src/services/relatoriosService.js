import { chamadosService } from './chamadosService'
import { relatorioDosChamados } from './indicadores'

export const relatoriosService = {
  async gerar(dias) { return relatorioDosChamados(await chamadosService.listar(), dias) },
}
