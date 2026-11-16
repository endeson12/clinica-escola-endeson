const {test}=require('./harness.cjs');
test("Q01 Conteúdo hostil permanece texto no navegador",async({browser,base,tag,admin,teacher,student,p,st,turma,discipline,enrollment,activity,delivery,actor,person,create,get,queue,grade,ready,page,assert})=>{
const hostile='<img src=x onerror="window.qaInjected=1">';
const a=await create(teacher,'atividades',{turmaId:turma,titulo:hostile,instrucoes:hostile,prazo:'2099-12-31T23:59:00Z'});
const e=await create(student,'entregas',{atividadeId:a,conteudo:hostile});await grade(8,hostile,true,teacher,e);
const ui=await page(student,'entregas?atividade='+a+'&situacao=PUBLICADA');
assert.match(await ui.locator('#table-results').innerText(),/<img src=x/);assert.equal(await ui.locator('#conteudo img').count(),0);
await ui.locator('[data-action="read-delivery"]').click();assert.match(await ui.locator('#dialog-body').innerText(),/<img src=x/);assert.equal(await ui.locator('#dialog-body img').count(),0);assert.equal(await ui.evaluate(()=>window.qaInjected),undefined);
assert.equal((await get(student,'boletim')).find(r=>r.id===e).feedback,hostile);
}).catch(e=>{console.error(e);process.exitCode=1});
