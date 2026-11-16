const {test}=require('./harness.cjs');
test("F05 Remover atividade mantém situação e busca",async({page,open,ready,requests,assert})=>{
await open('entregas?atividade=77&busca=QA&situacao=PENDENTE&pagina=2');await page.getByRole('link',{name:'Todas as atividades',exact:true}).click();await ready();await page.waitForFunction(()=>!location.hash.includes('atividade='));assert(page.url().includes('situacao=PENDENTE'));assert(page.url().includes('busca=QA'));assert(!page.url().includes('pagina='));assert(requests.some(q=>q==='situacao=PENDENTE'));
}).catch(e=>{console.error(e);process.exitCode=1});
