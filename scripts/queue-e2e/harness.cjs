// Real isolated HTTP server, PostgreSQL and Chromium. No mocked API.
const {chromium}=require('playwright');
const assert=require('node:assert/strict');
const base=process.env.QA_BASE;
assert(base&&/^http:\/\/127\.0\.0\.1:\d+$/.test(base),'Explicit loopback QA_BASE required');
async function test(name,fn) {
 const browser=await chromium.launch({executablePath:process.env.CHROME_PATH||'/usr/bin/google-chrome',args:['--no-sandbox']});
 const tag='qa-'+Date.now()+'-'+Math.random().toString(36).slice(2,7),password='SenhaTeste#123',errors=[];
 try {
  async function actor(email,unit=1) {
   const c=await browser.newContext();
   await c.request.get(base+'/api/auth/csrf');
   let token=(await c.cookies()).find(c=>c.name==='XSRF-TOKEN').value;
   const r=await c.request.post(base+'/api/auth/login',{headers:{'X-XSRF-TOKEN':token},data:{email,senha:password,unidadeId:unit}});assert.equal(r.status(),200);
   await c.request.get(base+'/api/auth/csrf');token=(await c.cookies()).find(c=>c.name==='XSRF-TOKEN').value;
   await c.setExtraHTTPHeaders({'X-XSRF-TOKEN':token});
   return c;
  }
  async function create(c,route,data) {
   const r=await c.request.post(base+'/api/academico/'+route,{data});assert.equal(r.status(),201,route+': '+await r.text());return (await r.json()).id;
  }
  const admin=await actor('admin.teste@clinica.local');
  async function person(role) {const email=tag+'-'+role+'-'+Math.random().toString(36).slice(2,7)+'@qa.local';return {id:await create(admin,'pessoas',{nome:tag+' '+role,email,senha:password,perfil:role}),context:await actor(email)};}
  const p=await person('PROFESSOR'),st=await person('ALUNO');const teacher=p.context,student=st.context;
  const course=await create(admin,'cursos',{nome:tag,codigo:tag,cargaHoraria:120});
  const discipline=await create(admin,'disciplinas',{cursoId:course,nome:tag,codigo:tag,cargaHoraria:60});
  const turma=await create(admin,'turmas',{disciplinaId:discipline,professorId:p.id,nome:tag,periodo:'2099.1',vagas:20});
  const enrollment=await create(admin,'matriculas',{turmaId:turma,alunoId:st.id});
  const activity=await create(teacher,'atividades',{turmaId:turma,titulo:tag,instrucoes:'Fixture QA isolada',prazo:'2099-12-31T23:59:00Z'});
  const delivery=await create(student,'entregas',{atividadeId:activity,conteudo:'Resposta QA original'});
  const get=async(c,path)=>{const r=await c.request.get(base+'/api/academico/'+path);assert.equal(r.status(),200,path);return r.json();};
  const queue=(c,state='')=>get(c,'entregas?atividadeId='+activity+'&situacao='+state);
  const grade=async(nota=8,feedback='Feedback QA',publicada=false,c=teacher,id=delivery)=>{
   const r=await c.request.put(base+'/api/academico/entregas/'+id+'/avaliacao',{data:{nota,feedback,publicada}});assert.equal(r.status(),200);return r.json();
  };
  const ready=page=>page.waitForFunction(()=>document.querySelector('#conteudo')?.getAttribute('aria-busy')==='false');
  const page=async(c=teacher,hash='entregas?atividade='+activity)=>{const page=await c.newPage();page.setDefaultTimeout(7000);page.on('pageerror',e=>errors.push(e.message));await page.goto(base+'/portal.html#'+hash);await ready(page);return page;};
  await fn({browser,base,tag,admin,teacher,student,p,st,turma,discipline,enrollment,activity,delivery,actor,person,create,get,queue,grade,ready,page,assert});
  assert.deepEqual(errors,[]);console.log('PASS '+name);
 } finally {await browser.close();}
}
module.exports={test};
