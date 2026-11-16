const {test}=require('./harness.cjs');
test("F08 Tabela com legenda de contexto",async({page,open,ready,requests,assert})=>{
await open();assert.equal(await page.locator('table caption').innerText(),'Entregas — registros desta unidade');assert.equal(await page.getByRole('table',{name:'Entregas — registros desta unidade'}).count(),1);
}).catch(e=>{console.error(e);process.exitCode=1});
