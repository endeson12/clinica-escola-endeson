const {test}=require('./harness.cjs');
test("F09 Ações da entrega têm nomes distintos",async({page,open,ready,requests,assert})=>{
await open();assert.equal(await page.getByRole('button',{name:'Avaliar entrega de João QA 1 — Avaliação de atenção 1',exact:true}).count(),1);assert.equal(await page.getByRole('button',{name:'Ler entrega de João QA 1 — Avaliação de atenção 1',exact:true}).count(),1);
}).catch(e=>{console.error(e);process.exitCode=1});
