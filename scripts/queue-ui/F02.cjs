const {test}=require('./harness.cjs');
test("F02 Busca tolera espaços nas extremidades",async({page,open,ready,requests,assert})=>{
await open('entregas?atividade=77&situacao=PENDENTE');const input=page.getByLabel('Buscar nesta lista');await input.fill('   QA   ');assert.equal(await page.locator('tbody tr').count(),1);assert(page.url().includes('atividade=77'));assert(page.url().includes('situacao=PENDENTE'));
}).catch(e=>{console.error(e);process.exitCode=1});
