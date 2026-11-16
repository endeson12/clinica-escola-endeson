const {test}=require('./harness.cjs');
test("F04 Limpar filtros mantém atividade",async({page,open,ready,requests,assert})=>{
await open('entregas?atividade=77&busca=Joao&situacao=PENDENTE&pagina=2');await page.getByRole('button',{name:'Limpar busca e situação',exact:true}).click();await ready();await page.waitForFunction(()=>!location.hash.includes('situacao=')&&!location.hash.includes('busca='));assert(page.url().includes('atividade=77'));assert(!page.url().includes('pagina='));assert.equal(await page.locator('tbody tr').count(),1);
}).catch(e=>{console.error(e);process.exitCode=1});
