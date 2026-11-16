const {test}=require('./harness.cjs');
test("F06 Vazio de avaliação diferencia filtro de cadastro",async({page,open,ready,requests,assert})=>{
await open('entregas?situacao=PUBLICADA');assert.match(await page.locator('#table-results').innerText(),/Nenhuma entrega nesta situação/);assert.match(await page.locator('#table-results').innerText(),/Altere a situação ou limpe os filtros/);
}, {emptyPublished:true}).catch(e=>{console.error(e);process.exitCode=1});
